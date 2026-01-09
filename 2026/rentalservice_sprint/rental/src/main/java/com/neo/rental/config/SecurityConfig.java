package com.neo.rental.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. 암호화 모듈 등록 (이게 있어야 Service에서 갖다 씁니다)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. HTTP 요청 권한 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf((csrf) -> csrf.disable()) // 실습용이라 csrf 보호 비활성화 (나중엔 켜는 게 좋음)
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/", "/member/save", "/member/login", "/css/**").permitAll() // 이 주소들은 로그인 없이 접속 허용
                        .anyRequest().authenticated() // 나머지 주소는 로그인해야 접속 가능
                );

        return http.build();
    }
}