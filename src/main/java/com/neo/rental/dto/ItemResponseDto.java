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
    private ItemStatus itemStatus;
    private LocalDateTime createdAt;

    // 👇 [추가] 프론트로 내려줄 좌표 정보
    private Double tradeLatitude;
    private Double tradeLongitude;
    private String tradeAddress;

    // 프론트엔드 요청 구조: item.owner.email ...
    private OwnerInfo owner;

    public ItemResponseDto(ItemEntity item) {
        this.itemId = item.getId();
        this.title = item.getTitle();
        this.content = item.getContent();
        this.price = item.getPrice();
        this.location = item.getLocation();
        this.itemImageUrl = item.getItemImageUrl();
        this.itemStatus = item.getItemStatus();
        this.createdAt = item.getCreatedAt();

        // 👇 [추가] 엔티티에서 좌표 꺼내기
        this.tradeLatitude = item.getTradeLatitude();
        this.tradeLongitude = item.getTradeLongitude();
        this.tradeAddress = item.getTradeAddress();

        // [핵심] 주인 정보 주입
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

        public Long getId() { return id; }
        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getPhone() { return phone; }
        public String getAddress() { return address; }
    }
}