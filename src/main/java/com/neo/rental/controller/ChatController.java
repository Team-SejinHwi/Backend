package com.neo.rental.controller;

import com.fasterxml.jackson.databind.ObjectMapper; //
import com.neo.rental.dto.ChatMessageDto;
import com.neo.rental.dto.ChatRoomDto;
import com.neo.rental.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessageSendingOperations messagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // [3-1] 채팅방 생성 또는 입장
    @PostMapping("/api/chat/room")
    public ResponseEntity<?> createRoom(@RequestBody Map<String, Long> payload, Principal principal) {
        Long itemId = payload.get("itemId");
        Long roomId = chatService.createOrGetChatRoom(itemId, principal.getName());

        Map<String, Object> data = new HashMap<>();
        data.put("roomId", roomId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "code", 200,
                "message", "채팅방이 개설되었습니다.",
                "data", data
        ));
    }

    // [변경] 추후 기능 추가 시 주석 해제
//    // [추가] 내 채팅방 목록 조회
//    @GetMapping("/api/chat/rooms")
//    public ResponseEntity<?> getMyChatRooms(Principal principal) {
//        List<ChatRoomDto> rooms = chatService.findAllRoom(principal.getName());
//        return ResponseEntity.ok(Map.of("success", true, "data", rooms));
//    }

    // [변경] HTML 테스트를 위한 변경점
    // [3-2] 실시간 메시지 전송 (Socket)
    // Destination: /pub/chat/message
    @MessageMapping("/chat/message")
    public void message(String rawMessage) { // String으로 받아서 수동 파싱
        log.info("📦 [Socket 수신 원본]: {}", rawMessage);

        try {
            // 수동 변환 시도
            ChatMessageDto messageDto = objectMapper.readValue(rawMessage, ChatMessageDto.class);
            log.info("✅ [DTO 변환 성공] RoomId: {}, Msg: {}", messageDto.getRoomId(), messageDto.getMessage());

            // DB 저장
            chatService.saveMessage(messageDto);

            // 구독자에게 전송
            messagingTemplate.convertAndSend("/sub/chat/room/" + messageDto.getRoomId(), messageDto);

        } catch (Exception e) {
            log.error("❌ [메시지 처리 실패] 원인: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}