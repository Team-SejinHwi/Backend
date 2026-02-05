package com.neo.rental.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // 👈 [추가] import 필수
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true) // 👈 [핵심] 알 수 없는 필드(예: timestamp 등)가 와도 에러 안 나게 무시함
public class ChatMessageDto {
    private Long roomId;
    private Long senderId;
    private String message;

    // 👇 [추가] 프론트엔드에서 보내는 "type": "TALK" 등을 받기 위한 필드
    private String type;
}