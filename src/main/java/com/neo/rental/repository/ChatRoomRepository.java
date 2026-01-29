package com.neo.rental.repository;

import com.neo.rental.entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {

    // [수정] findByItem_ItemId... -> findByItem_Id... (엔티티의 필드명인 'id' 사용)
    Optional<ChatRoomEntity> findByItem_IdAndBuyer_Id(Long itemId, Long buyerId);

    // 내가 참여중인 모든 방 찾기 (구매자 or 판매자)
    List<ChatRoomEntity> findByBuyer_IdOrSeller_Id(Long buyerId, Long sellerId);
}