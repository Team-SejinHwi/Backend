package com.neo.rental.dto;

import com.neo.rental.entity.ReviewEntity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewResponseDto {
    private Long reviewId;
    private String reviewerName; // 작성자 이름 (ID 대신 이름 노출)
    private int rating;
    private String content;
    private LocalDateTime createdAt;

    public ReviewResponseDto(ReviewEntity entity) {
        this.reviewId = entity.getId();
        this.reviewerName = entity.getReviewer().getName();
        this.rating = entity.getRating();
        this.content = entity.getContent();
        this.createdAt = entity.getCreatedAt();
    }
}