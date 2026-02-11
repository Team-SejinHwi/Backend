package com.neo.rental.service;

import com.neo.rental.constant.RentalStatus;
import com.neo.rental.entity.PaymentEntity;       // [필수 Import]
import com.neo.rental.entity.RentalEntity;
import com.neo.rental.repository.PaymentRepository; // [필수 Import]
import com.neo.rental.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final RentalRepository rentalRepository;

    // 👇 [여기가 빠져서 에러난 겁니다!] 이 줄이 있어야 DB에 저장이 가능합니다.
    private final PaymentRepository paymentRepository;

    // 토스 시크릿 키 (실무에선 application.yaml로 관리 권장)
    private final String tossSecretKey = "test_sk_P24xLea5zVA0yl1qD7X83QAMYNwW";

    @Transactional
    public String confirmPayment(Long rentalId, String paymentKey, String orderId, Long amount) {

        // 1. 렌탈 정보 조회
        RentalEntity rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 대여 ID입니다."));

        // 2. 금액 검증 (int vs Long 비교 주의)
        if (rental.getTotalPrice() != amount.intValue()) {
            throw new IllegalStateException("결제 금액이 일치하지 않습니다.");
        }

        // 3. 토스 API 호출 준비
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        String encodedAuth = Base64.getEncoder()
                .encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", paymentKey);
        body.put("orderId", orderId);
        body.put("amount", amount);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            // 4. 토스 승인 API 요청
            String response = restTemplate.postForObject(
                    "https://api.tosspayments.com/v1/payments/confirm",
                    request,
                    String.class
            );

            // 5. 렌탈 상태 변경: APPROVED -> PAID
            rental.setStatus(RentalStatus.PAID);

            // 6. [결제 이력 저장] PaymentEntity 생성 및 저장
            PaymentEntity payment = PaymentEntity.builder()
                    .rental(rental)
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(amount)
                    .status("DONE") // 결제 성공
                    .build();

            paymentRepository.save(payment);

            return response;

        } catch (Exception e) {
            log.error("토스 결제 승인 실패: {}", e.getMessage());
            throw new IllegalStateException("결제 승인 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}