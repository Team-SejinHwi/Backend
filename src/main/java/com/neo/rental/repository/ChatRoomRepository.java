package com.neo.rental.repository;

import com.neo.rental.entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {
    // 상품 ID + 구매자 ID로 이미 존재하는 방인지 확인
    Optional<ChatRoomEntity> findByItem_IdAndBuyer_Id(Long itemId, Long buyerId);

    // 내 채팅방 목록 조회 (구매자이거나 판매자인 경우 모두)
    List<ChatRoomEntity> findByBuyer_IdOrSeller_IdOrderByCreatedAtDesc(Long buyerId, Long sellerId);
}