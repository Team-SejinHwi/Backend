package com.neo.rental.entity;

import com.neo.rental.constant.RentalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "rental_table")
public class RentalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rental_id")
    private Long id;

    // ★ 어떤 물건? (필수)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private ItemEntity item;

    // ★ 누가 빌림? (필수)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_id", nullable = false)
    private MemberEntity renter;

    // 상태 (기본값 필요 시 Builder에서 처리)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalStatus status;

    // 날짜 정보는 필수
    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt; // 예약 신청 시간

    @UpdateTimestamp
    private LocalDateTime modifiedAt; // 상태 변경 시간 (수락/거절 등)
}