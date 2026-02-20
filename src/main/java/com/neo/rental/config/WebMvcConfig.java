package com.neo.rental.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // (기존에 있던 로컬 이미지 폴더 연결 설정은 S3 도입으로 인해 삭제되었습니다)

    // CORS 설정
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해
                .allowedOriginPatterns("*") // 모든 도메인 허용 (개발 단계 추천)
                // .allowedOrigins("http://localhost:3000", "https://your-vercel-app.vercel.app") // 배포 시 특정 도메인만 허용 권장
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // 허용할 HTTP 메서드
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true) // 쿠키/인증 정보 포함 허용
                .maxAge(3600); // 1시간 동안 Preflight 캐싱
    }
}