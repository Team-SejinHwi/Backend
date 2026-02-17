package com.neo.rental.repository;

import com.neo.rental.constant.RentalStatus;
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

    // 👇 [핵심] 새로고침 해도 버튼 잠그기 위함
    // "이 아이템(itemId)에 대해, 이 사람(email)이, 이 상태들(statuses) 중 하나라도 가지고 있니?"
    boolean existsByItem_IdAndRenter_EmailAndStatusIn(Long itemId, String renterEmail, List<RentalStatus> statuses);
}