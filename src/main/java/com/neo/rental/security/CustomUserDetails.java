package com.neo.rental.security;

import com.neo.rental.entity.MemberEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final MemberEntity memberEntity;

    public CustomUserDetails(MemberEntity memberEntity) {
        this.memberEntity = memberEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();

        // 롤(권한)이 있다면 여기서 추가. 현재는 비어있는 상태로 둡니다.
        // collection.add(new GrantedAuthority() { ... });

        return collection;
    }

    @Override
    public String getPassword() {
        // [수정] getMemberPassword() -> getPassword()
        return memberEntity.getPassword();
    }

    @Override
    public String getUsername() {
        // [수정] getMemberEmail() -> getEmail()
        // Spring Security에서는 "Username"이 로그인 ID(이메일)를 의미
        return memberEntity.getEmail();
    }

    // 사용자의 진짜 이름이 필요할 때 쓸 메서드 추가
    public String getName() {
        return memberEntity.getName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}