package com.neo.rental.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RentalRequestDto {
    private Long itemId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}