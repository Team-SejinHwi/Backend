package com.neo.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomListDto {
    private Long roomId;       // 채팅방 번호
    private Long itemId;       // 상품 번호
    private String itemTitle;  // 상품 제목
    private String itemImageUrl; // 상품 썸네일 (옵션)
    private String partnerName;// 대화 상대방 이름
    private String lastMessage;// (선택사항) 마지막 메시지 내용
}