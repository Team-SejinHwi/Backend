package com.neo.rental.constant;

public enum RentalStatus {
    WAITING,    // 대기중
    APPROVED,   // 승인됨
    PAID,       // 결제됨
    RENTING,    // 대여중
    RETURNED,   // 반납됨
    CANCELED,   // 취소됨 (사용자가 취소)
    REJECTED    // ★ 추가됨: 거절됨 (주인이 거절)
}