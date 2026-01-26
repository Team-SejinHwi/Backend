package com.neo.rental.controller;

import com.neo.rental.dto.ChatMessageDto;
import com.neo.rental.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessageSendingOperations messagingTemplate;

    // [API] 문의하기 버튼 클릭 -> 채팅방 ID 반환
    @PostMapping("/api/chat/room")
    public ResponseEntity<?> createRoom(@RequestBody Map<String, Long> payload, Principal principal) {
        Long itemId = payload.get("itemId");
        Long roomId = chatService.createOrGetChatRoom(itemId, principal.getName());
        return ResponseEntity.ok(Map.of("success", true, "roomId", roomId));
    }

    // [Socket] 메시지 전송 및 브로드캐스트
    // 클라이언트가 /pub/chat/message 로 보내면 동작
    @MessageMapping("/chat/message")
    public void message(ChatMessageDto messageDto) {
        // 1. DB 저장
        chatService.saveMessage(messageDto);

        // 2. 구독자에게 전송 (/sub/chat/room/{roomId})
        messagingTemplate.convertAndSend("/sub/chat/room/" + messageDto.getRoomId(), messageDto);
    }
}