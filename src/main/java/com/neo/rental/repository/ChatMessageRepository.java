package com.neo.rental.repository;

import com.neo.rental.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // List 임포트 필수

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    // [수정] Entity의 필드명이 'sendDate'이므로 메서드 이름도 'SendDate'여야 합니다.
    List<ChatMessageEntity> findAllByChatRoomIdOrderBySendDateAsc(Long roomId);
}