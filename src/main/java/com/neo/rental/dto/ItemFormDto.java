package com.neo.rental.dto;

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

    // 👇 [추가] 지도 좌표 및 주소 데이터
    private Double latitude;    // 프론트: latitude
    private Double longitude;   // 프론트: longitude
    private String address;     // 프론트: address (지도 선택 주소)
}