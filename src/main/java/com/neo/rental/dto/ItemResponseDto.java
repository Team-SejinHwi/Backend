package com.neo.rental.dto;

import com.neo.rental.constant.ItemStatus;
import com.neo.rental.entity.ItemEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class ItemResponseDto {

    private Long itemId;
    private String title;
    private String content;
    private Integer price;
    private String location;
    private String itemImageUrl;
    private ItemStatus itemStatus; // 판매 상태
    private LocalDateTime createdAt;

    // 주인 정보 (상세 페이지에서 보여줄 용도)
    private String ownerEmail;
    private String ownerName;

    // Entity -> DTO 변환을 위한 생성자
    public ItemResponseDto(ItemEntity item) {
        this.itemId = item.getId();
        this.title = item.getTitle();
        this.content = item.getContent();
        this.price = item.getPrice();
        this.location = item.getLocation();
        this.itemImageUrl = item.getItemImageUrl();
        this.itemStatus = item.getItemStatus();
        this.createdAt = item.getCreatedAt();

        // 주인 정보 꺼내오기 (N:1 관계라 item.getMember() 가능)
        this.ownerEmail = item.getMember().getEmail();
        this.ownerName = item.getMember().getName();
    }
}