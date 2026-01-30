package com.neo.rental.service;

import com.neo.rental.dto.ChatMessageDto;
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

    // 👇 [추가] 내 채팅방 목록 조회 (기존 Repository 활용)
    @Transactional(readOnly = true)
    public List<ChatRoomListDto> findAllRoom(String userEmail) {
        // 1. 내 정보(Member)를 먼저 찾아서 ID를 알아냄
        MemberEntity me = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));

        // 2. 내 ID가 구매자(Buyer)이거나 판매자(Seller)인 방을 모두 찾음
        //    (buyerId 자리에 내 ID, sellerId 자리에 내 ID를 넣어서 OR 검색)
        List<ChatRoomEntity> rooms = chatRoomRepository.findByBuyer_IdOrSeller_Id(me.getId(), me.getId());

        // 3. DTO로 변환
        return rooms.stream().map(room -> {
            String partnerName;

            // 상대방 이름 판별
            // 방의 구매자 ID가 내 ID와 같다면 -> 상대방은 판매자
            if (room.getBuyer().getId().equals(me.getId())) {
                partnerName = room.getSeller().getName();
            }
            // 아니라면 (내가 판매자) -> 상대방은 구매자
            else {
                partnerName = room.getBuyer().getName();
            }

            return ChatRoomListDto.builder()
                    .roomId(room.getId())
                    .itemId(room.getItem().getId())
                    .itemTitle(room.getItem().getTitle())
                    .itemImageUrl(room.getItem().getItemImageUrl()) // 이미지
                    .partnerName(partnerName)
                    .build();
        }).collect(Collectors.toList());
    }
}