package com.neo.rental.service;

import com.neo.rental.dto.MemberDTO;
import com.neo.rental.entity.MemberEntity;
import com.neo.rental.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public void save(MemberDTO memberDTO) {
        // [추가됨] 1. 중복 이메일 검증
        // (Repository에 boolean existsByEmail(String email) 메서드가 필요할 수 있음)
        if (memberRepository.findByEmail(memberDTO.getEmail()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }
        // 1. 사용자가 입력한 비밀번호(평문)를 꺼냄
        // (필드명이 password로 바뀌었으므로 getPassword() 사용)
        String originalPassword = memberDTO.getPassword();

        // 2. 암호화 진행
        String encodedPassword = passwordEncoder.encode(originalPassword);

        // 3. DTO에 암호화된 비밀번호를 다시 세팅
        memberDTO.setPassword(encodedPassword);

        // 4. 변환 및 저장 (DTO -> Entity)
        // Entity의 toMemberEntity 메서드 내부도 수정되어 있어야 합니다.
        MemberEntity memberEntity = MemberEntity.toMemberEntity(memberDTO);
        memberRepository.save(memberEntity);
    }

    // 로그인
    public MemberDTO login(MemberDTO memberDTO) {
        // 1. 이메일로 회원 조회
        // Entity 필드명이 email로 바뀌었으므로, Repository 메서드도 수정
        Optional<MemberEntity> byEmail = memberRepository.findByEmail(memberDTO.getEmail());

        if (byEmail.isPresent()) {
            MemberEntity memberEntity = byEmail.get();

            // 2. 비밀번호 비교 (입력받은 비번, DB 암호화 비번)
            if (passwordEncoder.matches(memberDTO.getPassword(), memberEntity.getPassword())) {
                // 비밀번호 일치 -> DTO 변환 후 리턴
                // DTO의 toMemberDTO 메서드 내부에서 비밀번호는 null 처리했으므로 안전
                return MemberDTO.toMemberDTO(memberEntity);
            } else {
                // 비밀번호 불일치
                return null;
            }
        } else {
            // 조회 결과 없음 (해당 이메일을 가진 회원이 없음)
            return null;
        }
    }
}