package com.neo.rental.entity;

import com.neo.rental.constant.ItemStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.neo.rental.constant.ItemCategory; // import 필수!

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

    // 제목: 100자 제한
    @Column(nullable = false, length = 100)
    private String title;

    // 내용: TEXT 타입
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // [추가] 카테고리 (DB에는 영어 문자열로 저장됨: "DIGITAL")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCategory category;

    @Column(nullable = false)
    private int price;

    // 기존 텍스트 위치 (예: 서울시 강남구)
    @Column(length = 100)
    private String location;

    // [추가] 좌표 및 상세 주소 저장용 컬럼
    @Column(name = "trade_latitude")
    private Double tradeLatitude;   // 위도 (y)

    @Column(name = "trade_longitude")
    private Double tradeLongitude;  // 경도 (x)

    @Column(name = "trade_address")
    private String tradeAddress;    // 지도에서 선택한 상세 주소

    // 이미지URL
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

    // 상품 정보 수정 메소드 (카테고리 업데이트 로직 추가 완료)
    public void updateItem(String title, ItemCategory category, String content, Integer price, String location, String itemImageUrl,
                           Double tradeLatitude, Double tradeLongitude, String tradeAddress) {
        this.title = title;
        this.category = category; // 👈 [핵심 수정] 이 줄이 있어야 DB값이 바뀝니다!
        this.content = content;
        this.price = price;
        this.location = location;
        this.itemImageUrl = itemImageUrl;

        this.tradeLatitude = tradeLatitude;
        this.tradeLongitude = tradeLongitude;
        this.tradeAddress = tradeAddress;
    }
}