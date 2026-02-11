package com.neo.rental.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_table")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String paymentKey; // 토스 결제 고유 키 (환불 때 필수!)

    @Column(nullable = false)
    private String orderId;    // 주문 ID (우리 서버에서 만든 랜덤 ID)

    @Column(nullable = false)
    private Long amount;       // 결제 금액

    @Column(nullable = false)
    private String status;     // DONE(성공), CANCELED(취소), ABORTED(실패)

    @CreatedDate
    private LocalDateTime paidAt; // 결제 일시

    // [중요] 어떤 대여 건인지 연결
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id")
    private RentalEntity rental;

    // 편의 메서드: 결제 취소 상태 변경
    public void cancel() {
        this.status = "CANCELED";
    }
}