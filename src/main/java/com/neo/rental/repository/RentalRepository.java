package com.neo.rental.repository;

import com.neo.rental.entity.RentalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RentalRepository extends JpaRepository<RentalEntity, Long> {

    // 1. 내가 빌린 내역 (마이페이지 -> 대여 내역)
    List<RentalEntity> findByRenterIdOrderByCreatedAtDesc(Long renterId);

    // 2. 내 물건에 들어온 예약 요청 (마이페이지 -> 예약 관리)
    // 렌탈에는 주인 ID가 없으므로, Item을 거쳐서 찾아야 함 (JPA의 강력한 기능)
    List<RentalEntity> findByItem_Member_IdOrderByCreatedAtDesc(Long memberId);

    // 3. 특정 물건의 예약 날짜 확인 (중복 예약 방지용)
    List<RentalEntity> findByItemId(Long itemId);
}