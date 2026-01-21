package com.neo.rental.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.http.SessionCreationPolicy; // 세션 정책 설정 시 필요

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 비활성화 (REST API 방식)
                .csrf((csrf) -> csrf.disable())

                // 2. CORS 설정 연결 (아래 corsConfigurationSource 메서드 적용)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))


                // [추가 제안] 3. 세션 정책 명시 (현재 세션 로그인 방식이라면 IF_REQUIRED)
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                // 3. HTTP 요청 권한 설정
                .authorizeHttpRequests((auth) -> auth
                                // [중요] Next.js에서 접근하는 로그인, 회원가입, 로그아웃 API 허용
                        .requestMatchers("/", "/api/auth/**" ,"/api/items/**", "/images/**").permitAll()
                                                                    // 임시 허용
                        // 정적 리소스 허용 (필요 시 유지)
                        .requestMatchers("/css/**", "/images/**", "/js/**", "/favicon.ico").permitAll()

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
//                                .anyRequest().permitAll()
                );

        return http.build();
    }

    // 4. CORS 설정 (Next.js와의 통신 허용)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // [수정] 특정 주소 대신 패턴 사용
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "https://*.ngrok-free.app",  // ★ 핵심: ngrok 주소 허용
                "https://*.ngrok-free.dev",
                "https://*.ngrok.io"         // (ngrok 구버전 주소 추가)
        ));

        configuration.setAllowedMethods(List.of("*")); // 모든 메서드 허용
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}