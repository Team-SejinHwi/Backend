package com.neo.rental.constant;

public enum RentalStatus {   // 임시 설정
    WAITING,    // 예약 신청 (주인 승인 대기중)
    APPROVED,   // 승인됨 (결제 전)
    PAID,       // 결제 완료 (예약 확정) - 추후 결제 붙일 때 사용
    RENTING,    // 대여 시작 (물건 건네받음)
    RETURNED,   // 반납 완료
    CANCELED    // 취소됨
}