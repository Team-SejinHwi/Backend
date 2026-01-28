package com.neo.rental.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "review_table")
public class ReviewEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    // 대여 1건당 리뷰 1개 (1:1 관계, Unique)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false, unique = true)
    private RentalEntity rental;

    // 상품과의 관계 (조회 편의성)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private ItemEntity item;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private MemberEntity reviewer;

    @Column(nullable = false)
    private int rating; // 1 ~ 5

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // ReviewEntity.java 안에 추가하세요

    // [수정 메서드]
    public void updateReview(Integer rating, String content) {
        this.rating = rating;
        this.content = content;
        // this.modifiedAt = LocalDateTime.now(); // BaseEntity 쓴다면 자동 처리됨
    }
}