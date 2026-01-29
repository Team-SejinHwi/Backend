package com.neo.rental.service;

import com.neo.rental.dto.ChatMessageDto;
import com.neo.rental.entity.*;
import com.neo.rental.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;

    // 1. 채팅방 생성 또는 조회 (기존 로직 유지)
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

    // 2. 메시지 저장 [수정됨: void -> ChatMessageEntity 반환]
    public ChatMessageEntity saveMessage(ChatMessageDto dto) { //
        ChatRoomEntity room = chatRoomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("방 없음"));

        MemberEntity sender = memberRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        ChatMessageEntity message = ChatMessageEntity.builder()
                .chatRoom(room)
                .senderId(sender.getId())
                .senderName(sender.getName()) // DB에서 조회한 정확한 이름 저장
                .message(dto.getMessage())
                .build();

        return chatMessageRepository.save(message); // 저장된 객체(시간 포함) 반환
    }
}