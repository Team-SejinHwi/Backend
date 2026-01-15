package com.neo.rental.repository;

import com.neo.rental.entity.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItemRepository extends JpaRepository<ItemEntity, Long> {

    // 메인 화면용: 최신 등록순 조회
    List<ItemEntity> findAllByOrderByCreatedAtDesc();

    // 지역별 조회 (예: 강남구 사는 사람끼리)
    List<ItemEntity> findByLocationContaining(String location);
}