package com.neo.rental.controller;

import com.neo.rental.dto.MemberDTO;
import com.neo.rental.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository; // 중요!
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 1. 회원가입 API (그대로 유지)
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody MemberDTO memberDTO) {
        try {
            memberService.save(memberDTO);
            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. 로그인 API (수정됨)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody MemberDTO memberDTO, HttpServletRequest request) {
        // A. 서비스에서 ID/PW 검증 (비밀번호 일치 여부 확인 필수!)
        MemberDTO loginResult = memberService.login(memberDTO);

        if (loginResult != null) {
            // [핵심 변경 사항] Spring Security에게 인증 정보 주입

            // 1. 인증 토큰 생성 (권한이 있다면 리스트에 추가, 여기선 빈 리스트)
            // 실제로는 new SimpleGrantedAuthority("ROLE_USER") 등을 넣어야 함
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(loginResult.getEmail(), null, Collections.emptyList());

            // 2. 시큐리티 컨텍스트 생성 및 토큰 설정
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authToken);
            SecurityContextHolder.setContext(securityContext);

            // 3. [중요] 세션에 시큐리티 컨텍스트 저장 (이게 없으면 다음 요청 때 로그인 풀림)
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

            return ResponseEntity.ok(loginResult);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("이메일 또는 비밀번호 불일치");
        }
    }

    // 3. 로그아웃 API (수정됨)
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        // 시큐리티 컨텍스트 비우기
        SecurityContextHolder.clearContext();

        // 세션 삭제
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}