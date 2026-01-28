package com.neo.rental.service;

import com.neo.rental.dto.MemberDTO;
import com.neo.rental.dto.MemberUpdateDto;
import com.neo.rental.dto.PasswordUpdateDto;
import com.neo.rental.dto.TokenInfo;
import com.neo.rental.entity.MemberEntity;
import com.neo.rental.jwt.JwtTokenProvider;
import com.neo.rental.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;

    // 1. 회원가입
    @Transactional
    public void save(MemberDTO memberDTO) {
        if (memberRepository.findByEmail(memberDTO.getEmail()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }
        String encodedPassword = passwordEncoder.encode(memberDTO.getPassword());
        memberDTO.setPassword(encodedPassword);
        memberRepository.save(MemberEntity.toMemberEntity(memberDTO));
    }

    // 2. 로그인 (수정됨: DB에 Refresh Token 저장)
    @Transactional // [중요] 변경감지(Dirty Checking)로 토큰 저장을 위해 읽기전용 해제
    public TokenInfo login(String email, String password) {
        // 1. 인증
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 2. 토큰 발급 (Access + Refresh)
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

        // 3. [추가] 리프레시 토큰 DB에 저장
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보가 없습니다."));
        member.updateRefreshToken(tokenInfo.getRefreshToken()); // Entity에 메서드 필요

        return tokenInfo;
    }

    // [신규] 3. 토큰 갱신 (Access Token 재발급)
    @Transactional
    public String refreshAccessToken(String refreshToken) {
        // 1. 리프레시 토큰 유효성 검사
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        // 2. DB에서 리프레시 토큰으로 회원 찾기
        MemberEntity member = memberRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("토큰이 만료되었거나 존재하지 않는 회원입니다."));

        // 3. 토큰 재발급 (Access Token만 새로 생성)
        // (편의상 Authentication 객체를 수동으로 생성하여 Provider에 넘김)
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(member.getEmail(), null, Collections.emptyList());

        TokenInfo newTokenInfo = jwtTokenProvider.generateToken(authenticationToken);

        // 리프레시 토큰도 같이 갱신하고 싶다면 여기서 member.updateRefreshToken(...) 수행
        // 현재는 Access Token만 반환
        return newTokenInfo.getAccessToken();
    }

    // 4. 내 정보 조회
    public MemberDTO getMyInfo(String email) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보가 없습니다."));
        return MemberDTO.toMemberDTO(member);
    }

    // 5. 내 정보 수정
    @Transactional
    public void updateMemberInfo(String email, MemberUpdateDto updateDto) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보가 없습니다."));
        member.updateMember(updateDto.getName(), updateDto.getPhone(), updateDto.getAddress());
    }

    // 6. 비밀번호 변경
    @Transactional
    public void updatePassword(String email, PasswordUpdateDto passDto) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보가 없습니다."));

        if (!passwordEncoder.matches(passDto.getCurrentPassword(), member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        String encodedNewPassword = passwordEncoder.encode(passDto.getNewPassword());
        member.updatePassword(encodedNewPassword);
    }
}