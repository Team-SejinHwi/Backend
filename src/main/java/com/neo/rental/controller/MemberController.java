package com.neo.rental.controller;

import com.neo.rental.dto.MemberDTO;
import com.neo.rental.dto.PasswordUpdateDto;
import com.neo.rental.dto.MemberUpdateDto;
import com.neo.rental.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;

import java.security.Principal;
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
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // 시큐리티 컨텍스트 비우기
        SecurityContextHolder.clearContext();

        // 세션 삭제
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // [추가된 부분] 3. 브라우저의 JSESSIONID 쿠키 강제 삭제 요청
        // "JSESSIONID"라는 이름의 쿠키를 덮어쓰는데, 수명을 0초로 설정해서 즉시 만료시킴
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");       // 모든 경로에서 삭제
        cookie.setMaxAge(0);       // 수명을 0으로 설정 (삭제)
        cookie.setHttpOnly(true);  // 보안 설정 (필수 아님, 기존 설정 따라감)

        response.addCookie(cookie);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    // 4. 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        // principal.getName()에는 로그인할 때 넣은 'email'이 들어있습니다.
        MemberDTO myInfo = memberService.getMyInfo(principal.getName());
        return ResponseEntity.ok(myInfo);
    }

    // 5. 내 정보 수정 (이름, 주소, 전화번호)
    @PutMapping("/me")
    public ResponseEntity<?> updateMyInfo(@RequestBody MemberUpdateDto updateDto, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            memberService.updateMemberInfo(principal.getName(), updateDto);
            return ResponseEntity.ok("회원 정보 수정 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 6. 비밀번호 변경
    @PatchMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody PasswordUpdateDto passDto, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            memberService.updatePassword(principal.getName(), passDto);
            return ResponseEntity.ok("비밀번호 변경 완료");
        } catch (IllegalArgumentException e) {
            // 비밀번호 불일치 등 명확한 에러 메시지 반환
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("비밀번호 변경 실패");
        }
    }
}