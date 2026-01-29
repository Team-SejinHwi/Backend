package com.neo.rental.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatRoomDto {
    private Long roomId;
    private String itemTitle;
    private String partnerName; // 상대방 이름
    private String lastMessage; // (선택사항) 마지막 대화 내용
}