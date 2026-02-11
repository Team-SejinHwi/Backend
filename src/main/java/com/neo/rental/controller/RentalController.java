package com.neo.rental.controller;

import com.neo.rental.dto.RentalDecisionDto;
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

    private Map<String, Object> createResponse(int code, String message, Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("code", code);
        response.put("message", message);
        if (data != null) response.put("data", data);
        return response;
    }

    // 1. 대여 신청
    @PostMapping
    public ResponseEntity<?> createRental(@RequestBody RentalRequestDto requestDto, Principal principal) {
        RentalResponseDto result = rentalService.createRental(principal.getName(), requestDto);
        return ResponseEntity.ok(createResponse(200, "대여 신청이 완료되었습니다.", result));
    }

    // 2. 내 대여 내역
    @GetMapping("/my")
    public ResponseEntity<?> getMyRentals(Principal principal) {
        List<RentalResponseDto> list = rentalService.getMyRentals(principal.getName());
        return ResponseEntity.ok(createResponse(200, "내 대여 내역 조회 성공", list));
    }

    // 3. 받은 예약 요청
    @GetMapping("/requests")
    public ResponseEntity<?> getReceivedRequests(Principal principal) {
        List<RentalResponseDto> list = rentalService.getReceivedRequests(principal.getName());
        return ResponseEntity.ok(createResponse(200, "받은 요청 목록 조회 성공", list));
    }

    // 4. 승인 및 거절 처리
    @PostMapping("/{rentalId}/decision")
    public ResponseEntity<?> handleDecision(@PathVariable Long rentalId, @RequestBody RentalDecisionDto decisionDto, Principal principal) {
        RentalResponseDto result = rentalService.handleDecision(rentalId, principal.getName(), decisionDto);
        String msg = decisionDto.isApproved() ? "예약이 승인되었습니다. (결제 대기)" : "예약이 거절되었습니다.";
        return ResponseEntity.ok(createResponse(200, msg, result));
    }

    // [NEW] 5. 대여 시작 (인계 확인) - 주인이 호출
    @PostMapping("/{rentalId}/start")
    public ResponseEntity<?> startRental(@PathVariable Long rentalId, Principal principal) {
        RentalResponseDto result = rentalService.startRental(rentalId, principal.getName());
        return ResponseEntity.ok(createResponse(200, "대여가 시작되었습니다. (인계 확인)", result));
    }

    // 6. 취소
    @PostMapping("/{rentalId}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long rentalId, Principal principal) {
        RentalResponseDto result = rentalService.cancelRental(rentalId, principal.getName());
        return ResponseEntity.ok(createResponse(200, "예약이 취소되었습니다.", result));
    }

    // 7. 반납 완료 처리
    @PostMapping("/{rentalId}/return")
    public ResponseEntity<?> returnItem(@PathVariable Long rentalId, Principal principal) {
        RentalResponseDto result = rentalService.returnItem(rentalId, principal.getName());
        return ResponseEntity.ok(createResponse(200, "반납이 완료되었습니다.", result));
    }
}