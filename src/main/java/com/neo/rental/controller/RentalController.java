package com.neo.rental.controller;

import com.neo.rental.dto.RentalDecisionDto;
import com.neo.rental.dto.RentalRequestDto;
import com.neo.rental.dto.RentalResponseDto;
import com.neo.rental.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    // JSON 응답 헬퍼
    private Map<String, Object> createResponse(int code, String message, Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("code", code);
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }

    // 1. 대여 신청 (POST /api/rentals)
    @PostMapping
    public ResponseEntity<?> createRental(@RequestBody RentalRequestDto requestDto, Principal principal) {
        RentalResponseDto result = rentalService.createRental(principal.getName(), requestDto);
        return ResponseEntity.ok(createResponse(200, "대여 신청이 완료되었습니다.", result));
    }

    // 2. 내 대여 내역 (GET /api/rentals/my)
    @GetMapping("/my")
    public ResponseEntity<?> getMyRentals(Principal principal) {
        List<RentalResponseDto> list = rentalService.getMyRentals(principal.getName());
        return ResponseEntity.ok(createResponse(200, "내 대여 내역 조회 성공", list));
    }

    // 3. 받은 예약 요청 (GET /api/rentals/requests)
    @GetMapping("/requests")
    public ResponseEntity<?> getReceivedRequests(Principal principal) {
        List<RentalResponseDto> list = rentalService.getReceivedRequests(principal.getName());
        return ResponseEntity.ok(createResponse(200, "받은 요청 목록 조회 성공", list));
    }

    // 4. 승인 및 거절 처리 (통합)
    // URL: POST /api/rentals/{rentalId}/decision
    @PostMapping("/{rentalId}/decision")
    public ResponseEntity<?> handleDecision(
            @PathVariable Long rentalId,
            @RequestBody RentalDecisionDto decisionDto, // ★ 만들어둔 DTO 사용
            Principal principal) {

        // 서비스 호출
        RentalResponseDto result = rentalService.handleDecision(rentalId, principal.getName(), decisionDto);

        // 응답 메시지 결정 (승인이면 "승인", 거절이면 "거절")
        String msg = decisionDto.isApproved() ? "예약이 승인되었습니다." : "예약이 거절되었습니다.";

        return ResponseEntity.ok(createResponse(true, msg, result));
    }

    // 5. 취소 (POST /api/rentals/{id}/cancel)
    @PostMapping("/{rentalId}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long rentalId, Principal principal) {
        RentalResponseDto result = rentalService.cancelRental(rentalId, principal.getName());
        return ResponseEntity.ok(createResponse(200, "예약이 취소되었습니다.", result));
    }

    // ==========================================
    // 유틸리티 메서드 (JSON 응답 생성용)
    // ==========================================
    private Map<String, Object> createResponse(boolean success, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put("data", data);

        // API 명세서에 code도 있었으므로, success면 200, 아니면 400으로 자동 설정
        response.put("code", success ? 200 : 400);

        return response;
    }
}
