package com.neo.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor; // [필수] [추가]

@Data
@NoArgsConstructor // [변경]
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private Long roomId;
    private Long senderId;
    private String message;
}