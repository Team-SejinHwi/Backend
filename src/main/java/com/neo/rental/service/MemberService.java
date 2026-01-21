package com.neo.rental.service;

import com.neo.rental.dto.MemberDTO;
import com.neo.rental.dto.MemberUpdateDto;
import com.neo.rental.dto.PasswordUpdateDto;
import com.neo.rental.entity.MemberEntity;
import com.neo.rental.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // [중요] 트랜잭션 처리를 위해 추가

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // [Tip] 기본적으로 읽기 전용으로 설정하여 성능 최적화
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. 회원가입
    @Transactional // [필수] 데이터 저장이 일어나므로 쓰기 트랜잭션 허용
    public void save(MemberDTO memberDTO) {
        // 중복 이메일 검증
        if (memberRepository.findByEmail(memberDTO.getEmail()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(memberDTO.getPassword());
        memberDTO.setPassword(encodedPassword);

        // 변환 및 저장
        MemberEntity memberEntity = MemberEntity.toMemberEntity(memberDTO);
        memberRepository.save(memberEntity);
    }

    // 2. 로그인 (읽기 전용이므로 @Transactional 생략 가능 - 클래스 레벨 적용됨)
    public MemberDTO login(MemberDTO memberDTO) {
        Optional<MemberEntity> byEmail = memberRepository.findByEmail(memberDTO.getEmail());

        if (byEmail.isPresent()) {
            MemberEntity memberEntity = byEmail.get();
            // 비밀번호 비교
            if (passwordEncoder.matches(memberDTO.getPassword(), memberEntity.getPassword())) {
                return MemberDTO.toMemberDTO(memberEntity);
            }
        }
        return null; // 로그인 실패
    }

    // [추가] 3. 내 정보 조회 (Controller에서 호출함)
    public MemberDTO getMyInfo(String email) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보가 없습니다."));
        return MemberDTO.toMemberDTO(member);
    }

    // 4. 내 정보 수정
    @Transactional // [필수] 이게 있어야 update 쿼리가 날아갑니다 (Dirty Checking)
    public void updateMemberInfo(String email, MemberUpdateDto updateDto) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보가 없습니다."));

        // Entity 내부의 값만 변경하면, 트랜잭션이 끝날 때 자동으로 DB 업데이트됨
        member.updateMember(updateDto.getName(), updateDto.getPhone(), updateDto.getAddress());
    }

    // 5. 비밀번호 변경
    @Transactional // [필수]
    public void updatePassword(String email, PasswordUpdateDto passDto) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보가 없습니다."));

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(passDto.getCurrentPassword(), member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 암호화 및 변경
        String encodedNewPassword = passwordEncoder.encode(passDto.getNewPassword());
        member.updatePassword(encodedNewPassword);
    }
}