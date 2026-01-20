package com.neo.rental.entity;

import com.neo.rental.constant.ItemStatus;
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
@Table(name = "item_table")
public class ItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    // 제목: 너무 길면 UI 깨지고 검색 느려짐 -> 100자 제한
    @Column(nullable = false, length = 100)
    private String title;

    // 내용: 사용자가 소설을 쓸 수도 있음 -> TEXT 타입 (약 6만자)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private int price;

    // 위치: "서울시 강남구 역삼동" 정도 -> 100자면 충분
    @Column(length = 100)
    private String location;

    // 이미지URL: 클라우드 URL은 엄청 김 -> TEXT 타입
    @Column(columnDefinition = "TEXT")
    private String itemImageUrl;

    @Enumerated(EnumType.STRING)
    private ItemStatus itemStatus;

    // ★ 물건 주인 (Member)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private MemberEntity member;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime modifiedAt;

    // 상품 정보 수정 메소드
    public void updateItem(String title, String content, Integer price, String location, String itemImageUrl) {
        this.title = title;
        this.content = content;
        this.price = price;
        this.location = location;
        this.itemImageUrl = itemImageUrl;
        // 필요하다면 status나 날짜 등도 여기서 변경
    }
}