package com.neo.rental.controller;

import com.neo.rental.dto.PaymentDto;
import com.neo.rental.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 결제 승인 요청 (프론트엔드에서 결제 성공 후 호출)
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody PaymentDto dto) {
        // 서비스 호출 (Toss API 연동 + 상태 변경)
        String tossResponse = paymentService.confirmPayment(
                dto.getRentalId(),
                dto.getPaymentKey(),
                dto.getOrderId(),
                dto.getAmount()
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "결제가 완료되었습니다.");
        response.put("tossResponse", tossResponse); // 디버깅용 (필요 없으면 제거 가능)

        return ResponseEntity.ok(response);
    }
}