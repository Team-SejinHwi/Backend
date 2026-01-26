package com.neo.rental.controller;

import com.neo.rental.dto.RentalRequestDto;
import com.neo.rental.dto.RentalResponseDto;
import com.neo.rental.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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

    // 4-1. 승인 (POST /api/rentals/{id}/approve)
    @PostMapping("/{rentalId}/approve")
    public ResponseEntity<?> approve(@PathVariable Long rentalId, Principal principal) {
        RentalResponseDto result = rentalService.handleDecision(rentalId, principal.getName(), true);
        return ResponseEntity.ok(createResponse(200, "예약이 승인되었습니다.", result));
    }

    // 4-2. 거절 (POST /api/rentals/{id}/reject)
    @PostMapping("/{rentalId}/reject")
    public ResponseEntity<?> reject(@PathVariable Long rentalId, Principal principal) {
        // 거절 로직이지만 Enum에 REJECTED가 없어 CANCELED 상태로 변경됨
        RentalResponseDto result = rentalService.handleDecision(rentalId, principal.getName(), false);
        return ResponseEntity.ok(createResponse(200, "예약이 거절(취소)되었습니다.", result));
    }

    // 5. 취소 (POST /api/rentals/{id}/cancel)
    @PostMapping("/{rentalId}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long rentalId, Principal principal) {
        RentalResponseDto result = rentalService.cancelRental(rentalId, principal.getName());
        return ResponseEntity.ok(createResponse(200, "예약이 취소되었습니다.", result));
    }
}