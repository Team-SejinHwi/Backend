package com.neo.rental.service;

import com.neo.rental.dto.ChatMessageDto;
import com.neo.rental.dto.ChatMessageResponseDto; // [추가]
import com.neo.rental.dto.ChatRoomListDto;
import com.neo.rental.entity.*;
import com.neo.rental.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;

    // 1. 채팅방 생성 또는 조회 (유지)
    public Long createOrGetChatRoom(Long itemId, String buyerEmail) {
        MemberEntity buyer = memberRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음"));

        if (item.getMember().getEmail().equals(buyerEmail)) {
            throw new IllegalStateException("자신의 상품에는 문의할 수 없습니다.");
        }

        return chatRoomRepository.findByItem_IdAndBuyer_Id(itemId, buyer.getId())
                .map(ChatRoomEntity::getId)
                .orElseGet(() -> {
                    ChatRoomEntity room = ChatRoomEntity.builder()
                            .item(item)
                            .buyer(buyer)
                            .seller(item.getMember())
                            .build();
                    return chatRoomRepository.save(room).getId();
                });
    }

    // 2. [수정됨] 메시지 저장 (DTO 반환으로 변경)
    public ChatMessageResponseDto saveMessage(ChatMessageDto dto) {
        ChatRoomEntity room = chatRoomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("방 없음"));

        MemberEntity sender = memberRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        ChatMessageEntity message = ChatMessageEntity.builder()
                .chatRoom(room)
                .senderId(sender.getId())
                .senderName(sender.getName()) // DB 저장 시점의 이름 고정
                .message(dto.getMessage())
                .build();

        ChatMessageEntity savedMessage = chatMessageRepository.save(message);

        // [핵심] Entity를 DTO로 변환해서 반환 (Lazy Loading 에러 방지)
        return ChatMessageResponseDto.from(savedMessage, dto.getType());
    }

    // 3. 내 채팅방 목록 조회 (유지)
    @Transactional(readOnly = true)
    public List<ChatRoomListDto> findAllRoom(String userEmail) {
        MemberEntity me = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));

        List<ChatRoomEntity> rooms = chatRoomRepository.findByBuyer_IdOrSeller_Id(me.getId(), me.getId());

        return rooms.stream().map(room -> {
            String partnerName;
            if (room.getBuyer().getId().equals(me.getId())) {
                partnerName = room.getSeller().getName();
            } else {
                partnerName = room.getBuyer().getName();
            }

            return ChatRoomListDto.builder()
                    .roomId(room.getId())
                    .itemId(room.getItem().getId())
                    .itemTitle(room.getItem().getTitle())
                    .itemImageUrl(room.getItem().getItemImageUrl())
                    .partnerName(partnerName)
                    .build();
        }).collect(Collectors.toList());
    }

    // [수정] 채팅방 입장 시 이전 대화 내용 불러오기
    // 설명: Entity를 그대로 컨트롤러로 넘기면 JSON 변환 중 'item' 정보 조회 시 Lazy 에러가 터집니다.
    //       따라서 여기서 DTO로 싹 변환해서 내보내는 것이 정석입니다.
    @Transactional(readOnly = true)
    public List<ChatMessageResponseDto> getMessages(Long roomId) {
        // 1. DB에서 Entity 리스트 조회
        List<ChatMessageEntity> entities = chatMessageRepository.findAllByChatRoomIdOrderBySendDateAsc(roomId);

        // 2. Entity List -> DTO List 변환
        return entities.stream()
                .map(entity -> ChatMessageResponseDto.from(entity, "TALK"))
                // Tip: DB에 'type' 컬럼이 없다면 기본값 "TALK"로 설정 (이전 대화는 대부분 대화내용이므로)
                .collect(Collectors.toList());
    }
}