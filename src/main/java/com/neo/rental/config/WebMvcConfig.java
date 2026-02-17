package com.neo.rental.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 1. 정적 리소스(이미지) 핸들러 설정
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /images/** 로 들어오는 요청은 실제 로컬 폴더(file://...)로 연결
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadDir);
    }

    // 2. CORS 설정 (프론트엔드 연동 필수)
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