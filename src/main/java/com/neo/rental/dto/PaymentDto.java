package com.neo.rental.dto;

import lombok.Data;

@Data
public class PaymentDto {
    private Long rentalId;      // 대여 ID
    private String paymentKey;  // 토스 결제 키
    private String orderId;     // 주문 ID
    private Long amount;        // 결제 금액
}