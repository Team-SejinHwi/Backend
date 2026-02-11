package com.neo.rental.constant;

public enum RentalStatus {
    WAITING,    // 승인 대기 (신청 직후)
    APPROVED,   // 승인됨 (결제 대기중) - 주인이 수락함
    PAID,       // 결제 완료 (인계 대기중) - 구매자가 결제함
    RENTING,    // 대여중 (인계 완료) - 주인이 물건 건네줌
    RETURNED,   // 반납 완료
    CANCELED,   // 취소됨
    REJECTED    // 거절됨
}