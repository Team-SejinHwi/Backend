package com.neo.rental.dto;

import com.neo.rental.constant.RentalStatus;
import com.neo.rental.entity.RentalEntity;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
public class RentalResponseDto {
    private Long rentalId;
    private Long itemId;
    private String itemTitle;
    private String itemImageUrl; // 썸네일 표시용
    private String renterName;   // 신청자 이름
    private String ownerName;    // 주인 이름
    private RentalStatus status;
    private int totalPrice;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;

    // 👇 [추가] 거절 사유 필드 필수!
    private String rejectReason;

    public RentalResponseDto(RentalEntity rental) {
        this.rentalId = rental.getId();
        this.itemId = rental.getItem().getId();
        this.itemTitle = rental.getItem().getTitle();
        // ItemEntity에 itemImageUrl 필드가 있다고 가정
        this.itemImageUrl = rental.getItem().getItemImageUrl();

        this.renterName = rental.getRenter().getName();
        this.ownerName = rental.getItem().getMember().getName();
        this.status = rental.getStatus();
        this.startDate = rental.getStartDate();
        this.endDate = rental.getEndDate();
        this.createdAt = rental.getCreatedAt();

        this.totalPrice = rental.getTotalPrice();
        // 👇 [핵심 수정] 엔티티에 있는 거절 사유를 DTO에 담기
        this.rejectReason = rental.getRejectReason();
    }
}