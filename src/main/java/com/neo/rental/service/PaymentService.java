package com.neo.rental.service;

import com.neo.rental.constant.RentalStatus;
import com.neo.rental.entity.RentalEntity;
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

    // [중요] 토스 개발자 센터 시크릿 키 (실무에선 application.yaml 환경변수로 관리 필수!)
    private final String tossSecretKey = "test_sk_P24xLea5zVA0yl1qD7X83QAMYNwW";

    @Transactional
    public String confirmPayment(Long rentalId, String paymentKey, String orderId, Long amount) {

        // 1. 렌탈 정보 조회
        RentalEntity rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 대여 ID입니다."));

        // 2. [수정됨] 금액 검증 (int형은 != 로 비교해야 함)
        if (rental.getTotalPrice() != amount.intValue()) {
            throw new IllegalStateException("결제 금액이 일치하지 않습니다.");
        }

        // 3. 토스 API 호출 준비
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        // Basic Auth 설정 (시크릿키 + ":") Base64 인코딩
        String encodedAuth = Base64.getEncoder()
                .encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // 요청 바디
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

            // 5. 성공 시 렌탈 상태 변경: APPROVED -> PAID
            rental.setStatus(RentalStatus.PAID);

            return response;

        } catch (Exception e) {
            // 결제 실패 시 예외 처리
            log.error("토스 결제 승인 실패: {}", e.getMessage());
            throw new IllegalStateException("결제 승인 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}