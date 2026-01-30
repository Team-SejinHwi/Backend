package com.neo.rental.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neo.rental.dto.ChatMessageDto;
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

    // [3-2] 실시간 메시지 전송 (Socket)
    @MessageMapping("/chat/message")
    public void message(String rawMessage) {
        log.info("📦 [Socket 수신 원본]: {}", rawMessage);

        try {
            // 1. 수동 파싱
            ChatMessageDto messageDto = objectMapper.readValue(rawMessage, ChatMessageDto.class);

            // 2. [핵심 수정] 로그에 senderId도 같이 찍어서 눈으로 확인!
            log.info("🔍 [파싱 데이터 확인] RoomId: {}, SenderId: {}, Msg: {}",
                    messageDto.getRoomId(),
                    messageDto.getSenderId(),
                    messageDto.getMessage());

            // 3. [핵심 수정] ID가 없으면 Service로 넘기지 말고 여기서 멈춤 (에러 방지)
            if (messageDto.getRoomId() == null || messageDto.getSenderId() == null) {
                log.error("❌ [전송 실패] 필수 ID가 누락되었습니다. RoomId 또는 SenderId가 NULL입니다.");
                return; // 여기서 함수 종료 (Service 호출 안 함)
            }

            // 4. DB 저장 (이제 안전함)
            chatService.saveMessage(messageDto);

            // 5. 구독자에게 전송
            messagingTemplate.convertAndSend("/sub/chat/room/" + messageDto.getRoomId(), messageDto);

        } catch (Exception e) {
            log.error("❌ [메시지 처리 중 예외 발생]: {}", e.getMessage());
            e.printStackTrace();
        }
    }
    // 👇 [3-3] 내 채팅방 목록 조회 (구현 완료)
    @GetMapping("/api/chat/rooms")
    public ResponseEntity<?> getMyChatRooms(Principal principal) {
        // Service 호출
        List<ChatRoomListDto> rooms = chatService.findAllRoom(principal.getName());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "code", 200,
                "message", "채팅방 목록 조회 성공",
                "data", rooms
        ));
    }
}