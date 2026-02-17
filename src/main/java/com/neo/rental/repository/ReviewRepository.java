package com.neo.rental.repository;

import com.neo.rental.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    // 1. 특정 상품에 달린 리뷰 목록 조회 (최신순)
    List<ReviewEntity> findByItem_IdOrderByCreatedAtDesc(Long itemId);

    // 2. 특정 렌탈 건에 이미 리뷰가 있는지 확인 (중복 방지용)
    Optional<ReviewEntity> findByRental_Id(Long rentalId);

    // 👇 [추가] 특정 유저가 이 상품에 리뷰를 남긴 적이 있는지 확인 (isReviewed용)
    boolean existsByItem_IdAndReviewer_Email(Long itemId, String email);
}