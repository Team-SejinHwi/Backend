package com.neo.rental.config;

import com.neo.rental.jwt.JwtAuthenticationFilter;
import com.neo.rental.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor; // [추가] 생성자 주입용
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // [추가] 필터 순서 설정용
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // [추가] CSRF 설정용

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // [중요] final 필드 생성자 자동 생성
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 비활성화 (REST API는 CSRF 방어가 불필요)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. [핵심] 세션 정책: STATELESS (세션을 사용하지 않음)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. HTTP 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 허용할 경로들
                        .requestMatchers("/", "/api/auth/**", "/api/items/**", "/images/**", "/api/reviews/**","/ws-stomp/**").permitAll()
                        // 정적 리소스 허용
                        .requestMatchers("/css/**", "/images/**", "/js/**", "/favicon.ico").permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // 5. [핵심] JWT 인증 필터 추가
                // UsernamePasswordAuthenticationFilter(기본 로그인 필터) 앞에서 동작하도록 설정
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS 설정 (기존 유지)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // [변경]채팅 시스템 테스트를 위한 임시 조치
        configuration.setAllowedOriginPatterns(List.of("*"));
//        configuration.setAllowedOriginPatterns(List.of(
//                "http://localhost:3000",
//                "http://127.0.0.1:3000",
//                "https://*.ngrok-free.app",
//                "https://*.ngrok-free.dev",
//                "https://*.ngrok.io"
//        ));

        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        // JWT 사용 시 Authorization 헤더 노출이 필요할 수 있음
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}