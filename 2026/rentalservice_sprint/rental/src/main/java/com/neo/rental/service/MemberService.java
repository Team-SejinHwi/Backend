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
    private final PasswordEncoder passwordEncoder; // 코드가 추가됨 (설정파일에서 등록한 빈 주입)

    // 회원가입 (암호화하여 저장)
    public void save(MemberDTO memberDTO) {
        // 1. 사용자가 입력한 비밀번호를 꺼냄
        String originalPassword = memberDTO.getMemberPassword();

        // 2. 암호화 진행 (1234 -> $2a$10$t2...)
        String encodedPassword = passwordEncoder.encode(originalPassword);

        // 3. DTO에 암호화된 비밀번호를 다시 세팅 (혹은 Entity 변환 시 사용)
        memberDTO.setMemberPassword(encodedPassword);

        // 4. 변환 및 저장
        MemberEntity memberEntity = MemberEntity.toMemberEntity(memberDTO);
        memberRepository.save(memberEntity);
    }

    // 로그인 (암호화된 비번과 비교)
    public MemberDTO login(MemberDTO memberDTO) {
        // 1. 이메일로 회원 조회
        Optional<MemberEntity> byMemberEmail = memberRepository.findByMemberEmail(memberDTO.getMemberEmail());

        if (byMemberEmail.isPresent()) {
            MemberEntity memberEntity = byMemberEmail.get();

            // [중요] 2. 비밀번호 비교 (matches 메서드 사용 필수!)
            // passwordEncoder.matches(입력받은_평문_비번, DB에_저장된_암호화_비번)
            // 암호화된 비밀번호는 매번 달라도 내부적으로 해독해서 맞는지 확인해줌
            if (passwordEncoder.matches(memberDTO.getMemberPassword(), memberEntity.getMemberPassword())) {
                // 비밀번호 일치 -> DTO 변환 후 리턴 (비번 필드는 뺀 상태로)
                MemberDTO dto = MemberDTO.toMemberDTO(memberEntity);
                return dto;
            } else {
                // 비밀번호 불일치
                return null;
            }
        } else {
            // 조회 결과 없음
            return null;
        }
    }
}