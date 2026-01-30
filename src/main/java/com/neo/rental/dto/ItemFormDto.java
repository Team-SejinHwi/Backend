package com.neo.rental.dto;

import com.neo.rental.constant.ItemCategory;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
public class ItemFormDto {
    // 프론트에서 이 이름으로 JSON을 보내줘야 함
    private String title;       // 제목
    private String content;     // 내용
    private Integer price;      // 가격
    private String location;    // 지역 (텍스트 입력)
    private String itemImageUrl; // 이미지 주소

    // ▼ [추가] 프론트에서 "DIGITAL" 같은 문자열을 보내면 자동으로 Enum으로 매핑됨
    private ItemCategory category;

    private Double latitude;    // 프론트: latitude
    private Double longitude;   // 프론트: longitude
    private String address;     // 프론트: address (지도 선택 주소)
}