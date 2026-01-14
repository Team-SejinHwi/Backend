package com.neo.rental.security;

import com.neo.rental.entity.MemberEntity;
import com.neo.rental.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 1. 이메일로 회원 조회 (findByMemberEmail -> findByEmail)
        MemberEntity memberEntity = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 가진 회원이 존재하지 않습니다: " + email));

        // 2. UserDetails 구현체 반환
        // (주의: CustomUserDetails 내부에서도 getMemberEmail() 등을 getEmail()로 수정해야 함)
        return new CustomUserDetails(memberEntity);
    }
}