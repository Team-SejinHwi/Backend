package com.neo.rental.dto;

import lombok.Data;

@Data
public class ChatMessageDto {
    private Long roomId;    // 방 번호
    private Long senderId;  // 보낸 사람
    private String message; // 내용
}