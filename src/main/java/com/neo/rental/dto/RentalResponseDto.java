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
    private int totalPrice;      // ★ 계산된 총 가격
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;

    public RentalResponseDto(RentalEntity rental) {
        this.rentalId = rental.getId();
        this.itemId = rental.getItem().getId();
        this.itemTitle = rental.getItem().getTitle();
        // ItemEntity에 itemImageUrl 필드가 있다고 가정 (없으면 삭제)
        // this.itemImageUrl = rental.getItem().getItemImageUrl();

        this.renterName = rental.getRenter().getName();
        this.ownerName = rental.getItem().getMember().getName();
        this.status = rental.getStatus();
        this.startDate = rental.getStartDate();
        this.endDate = rental.getEndDate();
        this.createdAt = rental.getCreatedAt();

        // ★ 총 가격 계산 로직 (엔티티에 필드가 없으므로 여기서 계산)
        long days = ChronoUnit.DAYS.between(rental.getStartDate(), rental.getEndDate());
        if (days <= 0) days = 1; // 최소 1일치 계산
        this.totalPrice = rental.getItem().getPrice() * (int) days;
    }
}