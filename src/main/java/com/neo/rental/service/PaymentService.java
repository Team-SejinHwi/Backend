package com.neo.rental.service;

import com.neo.rental.constant.RentalStatus;
import com.neo.rental.entity.PaymentEntity;
import com.neo.rental.entity.RentalEntity;
import com.neo.rental.repository.PaymentRepository;
import com.neo.rental.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final PaymentRepository paymentRepository;

    // 👇 [수정] yml의 toss.secret-key (즉, 환경변수 TOSS_SECRET_KEY) 값을 주입받음
    @Value("${toss.secret-key}")
    private String tossSecretKey;

    @Transactional
    public String confirmPayment(Long rentalId, String paymentKey, String orderId, Long amount) {

        // 1. 렌탈 정보 조회
        RentalEntity rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 대여 ID입니다."));

        // 로그 확인
        log.info("💰 금액 검증 - DB가격: {}, 결제요청금액: {}", rental.getTotalPrice(), amount);

        // [중복 결제 방어] 이미 결제된 건(PAID)이면 성공 처리
        if (rental.getStatus() == RentalStatus.PAID) {
            log.info("이미 결제 완료된 건입니다. 중복 요청을 건너뜁니다. rentalId: {}", rentalId);
            return "{\"message\": \"이미 처리된 결제입니다.\", \"status\": \"DONE\"}";
        }

        // 2. 금액 검증
        if (rental.getTotalPrice() != amount.intValue()) {
            throw new IllegalStateException("결제 금액이 일치하지 않습니다.");
        }

        // 3. 토스 API 호출 준비
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        // Basic Auth 헤더 생성
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

            // 6. [결제 이력 저장]
            PaymentEntity payment = PaymentEntity.builder()
                    .rental(rental)
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(amount)
                    .status("DONE")
                    .build();

            paymentRepository.save(payment);

            return response;

        } catch (Exception e) {
            log.error("토스 결제 승인 실패: {}", e.getMessage());
            throw new IllegalStateException("결제 승인 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}