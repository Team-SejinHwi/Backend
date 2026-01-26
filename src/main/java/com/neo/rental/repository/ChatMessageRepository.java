package com.neo.rental.repository;

import com.neo.rental.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    // 추가 쿼리 필요 시 작성
}