package com.neo.rental.controller;

import com.neo.rental.dto.MemberDTO;
import com.neo.rental.dto.MemberUpdateDto;
import com.neo.rental.dto.PasswordUpdateDto;
import com.neo.rental.dto.TokenInfo; // [필수] TokenInfo 임포트 확인!
import com.neo.rental.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 1. 회원가입 API
    @PostMapping("/auth/signup")
    public ResponseEntity<String> signup(@RequestBody MemberDTO memberDTO) {
        try {
            memberService.save(memberDTO);
            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. 로그인 API (JWT 버전으로 수정)
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody MemberDTO memberDTO) {
        // [수정] 서비스의 login 메서드 시그니처에 맞게 호출 (DTO 통째로 x, 이메일/비번 분리 o)
        // [수정] 리턴 타입도 MemberDTO가 아니라 TokenInfo
        try {
            TokenInfo tokenInfo = memberService.login(memberDTO.getEmail(), memberDTO.getPassword());
            return ResponseEntity.ok(tokenInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패: " + e.getMessage());
        }
    }

    // 3. 로그아웃 API (JWT는 서버 세션이 없으므로 클라이언트가 토큰을 버리면 끝)
    @PostMapping("/auth/logout")
    public ResponseEntity<String> logout() {
        // 프론트엔드에서 localStorage의 토큰을 삭제하면 로그아웃입니다.
        // 서버에서는 딱히 할 일이 없지만, 응답을 위해 남겨둡니다.
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    // 4. 내 정보 조회
    @GetMapping("/members/me")
    public ResponseEntity<?> getMyInfo(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        MemberDTO myInfo = memberService.getMyInfo(principal.getName());
        return ResponseEntity.ok(myInfo);
    }

    // 5. 내 정보 수정
    @PutMapping("/members/me")
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
    @PatchMapping("/members/password")
    public ResponseEntity<?> updatePassword(@RequestBody PasswordUpdateDto passDto, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            memberService.updatePassword(principal.getName(), passDto);
            return ResponseEntity.ok("비밀번호 변경 완료");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("비밀번호 변경 실패");
        }
    }
}