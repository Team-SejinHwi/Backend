package com.neo.rental.repository;

import com.neo.rental.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    // 특정 방의 메시지 내역 조회 (필요 시 사용)
    // List<ChatMessageEntity> findByChatRoom_IdOrderBySendDateAsc(Long roomId);
}