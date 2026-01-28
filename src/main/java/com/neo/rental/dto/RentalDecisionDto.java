package com.neo.rental.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RentalDecisionDto {
    private boolean approved;   // true: 승인, false: 거절
    private String rejectReason; // 거절일 때만 값이 들어옴
}