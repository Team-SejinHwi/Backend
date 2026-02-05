package com.neo.rental.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neo.rental.dto.ChatMessageDto;
import com.neo.rental.dto.ChatMessageResponseDto; // [추가]
import com.neo.rental.dto.ChatRoomListDto;
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

    // [3-1] 채팅방 생성 또는 입장 (유지)
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

    // [3-2] 실시간 메시지 전송 (Socket) - [수정됨]
    @MessageMapping("/chat/message")
    public void message(String rawMessage) {
        log.info("📦 [Socket 수신 원본]: {}", rawMessage);

        try {
            // 1. 수동 파싱
            ChatMessageDto messageDto = objectMapper.readValue(rawMessage, ChatMessageDto.class);

            // 2. 로그 확인
            log.info("🔍 [파싱 데이터 확인] RoomId: {}, SenderId: {}, Msg: {}",
                    messageDto.getRoomId(),
                    messageDto.getSenderId(),
                    messageDto.getMessage());

            // 3. 유효성 검사
            if (messageDto.getRoomId() == null || messageDto.getSenderId() == null) {
                log.error("❌ [전송 실패] 필수 ID가 누락되었습니다.");
                return;
            }

            // 4. [수정] DB 저장 후 'DTO'를 반환받음 (Entity 아님!)
            ChatMessageResponseDto savedMessage = chatService.saveMessage(messageDto);

            // 5. [수정] 구독자에게 DTO 전송 (안전함)
            messagingTemplate.convertAndSend("/sub/chat/room/" + messageDto.getRoomId(), savedMessage);

        } catch (Exception e) {
            log.error("❌ [메시지 처리 중 예외 발생]: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    // [3-3] 내 채팅방 목록 조회 (유지)
    @GetMapping("/api/chat/rooms")
    public ResponseEntity<?> getMyChatRooms(Principal principal) {
        List<ChatRoomListDto> rooms = chatService.findAllRoom(principal.getName());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "code", 200,
                "message", "채팅방 목록 조회 성공",
                "data", rooms
        ));
    }
    // [3-4] 채팅방 이전 대화 내역 조회 (HTTP GET)
    // URL: /api/chat/room/{roomId}/messages
    @GetMapping("/api/chat/room/{roomId}/messages")
    public ResponseEntity<?> getRoomMessages(@PathVariable Long roomId) {

        // Service에서 이미 DTO 리스트로 변환되어 넘어옴 (안전함)
        List<ChatMessageResponseDto> messages = chatService.getMessages(roomId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "code", 200,
                "message", "이전 대화 내역 조회 성공",
                "data", messages
        ));
    }
}