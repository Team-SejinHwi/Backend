package com.neo.rental.repository;

import com.neo.rental.entity.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<ItemEntity, Long> {

    /**
     * [통합 검색 쿼리 - 리스트 버전]
     * 1. 카테고리 (일치)
     * 2. 키워드 (포함)
     * 3. 위치 (반경)
     * 4. 정렬 (거리순/최신순)
     * 5. 제한 (동적 Limit)
     */
    @Query(value = "SELECT * FROM item_table i " +
            "WHERE i.item_status = 'AVAILABLE' " +
            "AND (:category IS NULL OR i.category = :category) " +
            "AND (:keyword IS NULL OR i.title LIKE CONCAT('%', :keyword, '%')) " +
            "AND (" +
            "   (:lat IS NULL OR :lng IS NULL) " +
            "   OR ST_Distance_Sphere(POINT(:lng, :lat), POINT(i.trade_longitude, i.trade_latitude)) <= :radius" +
            ") " +
            "ORDER BY " +
            "   CASE WHEN (:lat IS NOT NULL AND :lng IS NOT NULL) " +
            "       THEN ST_Distance_Sphere(POINT(:lng, :lat), POINT(i.trade_longitude, i.trade_latitude)) " +
            "       ELSE i.created_at END " +
            "   ASC, i.created_at DESC " +
            "LIMIT :limit", // 👈 [수정] 동적 Limit 적용
            nativeQuery = true)
    List<ItemEntity> searchItems(
            @Param("category") String category,
            @Param("keyword") String keyword,
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("radius") Double radius,
            @Param("limit") int limit // 👈 [추가]
    );
}