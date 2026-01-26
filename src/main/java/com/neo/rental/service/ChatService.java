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

    // 1. 채팅방 생성 또는 조회
    public Long createOrGetChatRoom(Long itemId, String buyerEmail) {
        MemberEntity buyer = memberRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음"));

        // 자가 문의 방지
        if (item.getMember().getEmail().equals(buyerEmail)) {
            throw new IllegalStateException("자신의 상품에는 문의할 수 없습니다.");
        }

        // 이미 방이 있으면 ID 리턴, 없으면 만들어서 리턴
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

    // 2. 메시지 저장
    public void saveMessage(ChatMessageDto dto) {
        ChatRoomEntity room = chatRoomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("방 없음"));

        MemberEntity sender = memberRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        ChatMessageEntity message = ChatMessageEntity.builder()
                .chatRoom(room)
                .senderId(sender.getId())
                .senderName(sender.getName())
                .message(dto.getMessage())
                .build();

        chatMessageRepository.save(message);
    }
}