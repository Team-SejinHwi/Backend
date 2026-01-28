package com.neo.rental.dto;

import lombok.Data;

@Data
public class ReviewRequestDto {
    private Long rentalId; // 어떤 거래인지 식별
    private int rating;    // 별점 (1~5)
    private String content; // 내용
}