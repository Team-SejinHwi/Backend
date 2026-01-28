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

    // 프론트엔드 요청 구조: item.owner.email, item.owner.phone ...
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

        // [핵심] 주인 정보 주입
        if (item.getMember() != null) {
            // 이름 방어 로직
            String safeName = item.getMember().getName();
            if (safeName == null || safeName.trim().isEmpty()) safeName = "이름 없음";

            // ★ 전화번호, 주소도 꺼내서 넣어줌 (Entity에 해당 필드가 있어야 함)
            this.owner = new OwnerInfo(
                    item.getMember().getId(),
                    item.getMember().getEmail(),
                    safeName,
                    item.getMember().getPhone(),   // [추가] 전화번호
                    item.getMember().getAddress()  // [추가] 주소
            );
        } else {
            // 주인이 없는 경우 (빈 값으로 채움)
            this.owner = new OwnerInfo(-1L, "", "알 수 없음", "", "");
        }
    }

    // ★ [중요] 내부 클래스 (Lombok 대신 수동 Getter 사용)
    public static class OwnerInfo {
        private Long id;
        private String email;
        private String name;
        private String phone;   // [추가]
        private String address; // [추가]

        public OwnerInfo(Long id, String email, String name, String phone, String address) {
            this.id = id;
            this.email = email;
            this.name = name;
            this.phone = phone;
            this.address = address;
        }

        // ▼ JSON 변환을 위해 Getter 필수
        public Long getId() { return id; }
        public String getEmail() { return email; }
        public String getName() { return name; }

        // [추가된 Getter]
        public String getPhone() { return phone; }
        public String getAddress() { return address; }
    }
}