package com.neo.rental.repository;

import com.neo.rental.constant.RentalStatus; // [필수 Import]
import com.neo.rental.entity.RentalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RentalRepository extends JpaRepository<RentalEntity, Long> {

    // 1. 내가 빌린 내역
    List<RentalEntity> findByRenterIdOrderByCreatedAtDesc(Long renterId);
    // 2. 받은 예약 요청
    List<RentalEntity> findByItem_Member_IdOrderByCreatedAtDesc(Long memberId);
    // 3. 특정 물건 예약 확인
    List<RentalEntity> findByItemId(Long itemId);

    // 👇 [추가] 상세페이지 버튼 비활성화용 (특정 유저가, 특정 아이템을, 특정 상태들로 가지고 있는지 확인)
    boolean existsByItem_IdAndRenter_IdAndStatusIn(Long itemId, Long renterId, List<RentalStatus> statuses);
}