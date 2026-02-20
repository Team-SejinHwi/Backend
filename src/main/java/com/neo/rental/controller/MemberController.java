package com.neo.rental.controller;

import com.neo.rental.dto.MemberDTO;
import com.neo.rental.dto.MemberUpdateDto;
import com.neo.rental.dto.PasswordUpdateDto;
import com.neo.rental.dto.RefreshTokenRequestDto; // [필수] 임포트 확인
import com.neo.rental.dto.TokenInfo;
import com.neo.rental.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    // 2. 로그인 API
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody MemberDTO memberDTO) {
        try {
            // 이메일과 비밀번호로 로그인 시도 -> Access + Refresh Token 반환
            TokenInfo tokenInfo = memberService.login(memberDTO.getEmail(), memberDTO.getPassword());
            return ResponseEntity.ok(tokenInfo);
        } catch (Exception e) {
            // 콘솔에는 원래 에러를 찍어두어 개발자가 확인할 수 있게 합니다.
            e.printStackTrace();
            // 프론트엔드(사용자)에게는 친숙하고 부드러운 메시지를 보냅니다.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("이메일 또는 비밀번호가 일치하지 않습니다. 다시 확인해 주세요!");
        }
    }

    // 3. 로그아웃 API
    @PostMapping("/auth/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    // [신규] 4. 토큰 갱신 API (Refresh Token)
    @PostMapping("/auth/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequestDto requestDto) {
        try {
            // 서비스에서 리프레시 토큰 검증 후 새 엑세스 토큰 발급
            String newAccessToken = memberService.refreshAccessToken(requestDto.getRefreshToken());

            // 응답 포맷 생성 ({ success: true, data: { accessToken: ... } })
            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", newAccessToken);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 리프레시 토큰이 만료되었거나 유효하지 않은 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "토큰 갱신 실패: " + e.getMessage()));
        }
    }

    // 5. 내 정보 조회
    @GetMapping("/members/me")
    public ResponseEntity<?> getMyInfo(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        MemberDTO myInfo = memberService.getMyInfo(principal.getName());
        return ResponseEntity.ok(myInfo);
    }

    // 6. 내 정보 수정
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

    // 7. 비밀번호 변경
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