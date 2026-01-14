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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf((csrf) -> csrf.disable())
                .authorizeHttpRequests((auth) -> auth
                        // 정적 리소스(이미지, JS 등)도 접근 가능하게 추가하는 것이 좋습니다 (/images/**, /js/**)
                        .requestMatchers("/", "/rental/save", "/rental/login", "/rental/logout", "/css/**", "/images/**", "/js/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/rental/login")
                        .loginProcessingUrl("/rental/login")

                        // [중요 수정 포인트] HTML form의 <input name="..."> 값과 일치해야 합니다.
                        .usernameParameter("email")    // memberEmail -> email
                        .passwordParameter("password") // memberPassword -> password

                        .defaultSuccessUrl("/rental/main", true)
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutUrl("/rental/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                );

        return http.build();
    }
}