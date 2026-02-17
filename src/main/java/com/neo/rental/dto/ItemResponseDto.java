package com.neo.rental.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.neo.rental.constant.ItemCategory;
import com.neo.rental.constant.ItemStatus;
import com.neo.rental.entity.ItemEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class ItemResponseDto {

    private Long itemId;
    private String title;
    private String content;
    private Integer price;
    private String location;
    private String itemImageUrl;
    private ItemStatus itemStatus;
    private LocalDateTime createdAt;

    // 카테고리 정보
    private ItemCategory category;
    private String categoryName;

    // 좌표 정보
    private Double tradeLatitude;
    private Double tradeLongitude;
    private String tradeAddress;

    private OwnerInfo owner;

    // 리뷰 관련
    private List<ReviewResponseDto> reviews;
    private Double averageRating;
    private int reviewCount;

    // ✅ [신청 상태 필드] (True면 신청 버튼 비활성화)
    @JsonProperty("isRequested")
    private boolean isRequested;

    // 👇 [추가] [리뷰 작성 여부] (True면 리뷰 작성 버튼 숨김/비활성화)
    @JsonProperty("isReviewed")
    private boolean isReviewed;

    public ItemResponseDto(ItemEntity item) {
        this.itemId = item.getId();
        this.title = item.getTitle();
        this.content = item.getContent();
        this.price = item.getPrice();
        this.location = item.getLocation();
        this.itemImageUrl = item.getItemImageUrl();
        this.itemStatus = item.getItemStatus();
        this.createdAt = item.getCreatedAt();

        this.category = item.getCategory();
        this.categoryName = (item.getCategory() != null) ? item.getCategory().getDescription() : null;

        this.tradeLatitude = item.getTradeLatitude();
        this.tradeLongitude = item.getTradeLongitude();
        this.tradeAddress = item.getTradeAddress();

        if (item.getMember() != null) {
            String safeName = item.getMember().getName();
            if (safeName == null || safeName.trim().isEmpty()) safeName = "이름 없음";

            this.owner = new OwnerInfo(
                    item.getMember().getId(),
                    item.getMember().getEmail(),
                    safeName,
                    item.getMember().getPhone(),
                    item.getMember().getAddress()
            );
        } else {
            this.owner = new OwnerInfo(-1L, "", "알 수 없음", "", "");
        }
    }

    @Getter @Setter
    public static class OwnerInfo {
        private Long id;
        private String email;
        private String name;
        private String phone;
        private String address;

        public OwnerInfo(Long id, String email, String name, String phone, String address) {
            this.id = id;
            this.email = email;
            this.name = name;
            this.phone = phone;
            this.address = address;
        }
    }
}