package com.neo.rental.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.neo.rental.entity.ChatMessageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponseDto {
    private Long messageId;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String message;
    private String type; // TALK, ENTER 등

    // 날짜 포맷 예쁘게 설정 (선택사항)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime sendDate;

    // Entity -> DTO 변환 메서드 (편의용)
    public static ChatMessageResponseDto from(ChatMessageEntity entity, String type) {
        return ChatMessageResponseDto.builder()
                .messageId(entity.getId())
                .roomId(entity.getChatRoom().getId()) // 여기서 ID만 꺼내므로 Lazy Loading 에러 안 남
                .senderId(entity.getSenderId())
                .senderName(entity.getSenderName())
                .message(entity.getMessage())
                .sendDate(entity.getSendDate())
                .type(type)
                .build();
    }
}