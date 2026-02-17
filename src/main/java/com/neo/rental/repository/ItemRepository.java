package com.neo.rental.repository;

import com.neo.rental.entity.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<ItemEntity, Long> {

    /**
     * [통합 검색 쿼리 - 리스트 버전]
     * 수정사항: item_status 조건을 'AVAILABLE' 단일 체크에서
     * ('AVAILABLE', 'RENTED') 포함 체크로 변경하거나,
     * 상태 조건을 제거하여 모든 상품을 노출시킴.
     */
    @Query(value = "SELECT * FROM item_table i " +
            "WHERE i.item_status IN ('AVAILABLE', 'RENTED','SOLD_OUT') " + // 👈 [핵심 수정] RENTED 상태도 조회 목록에 포함!
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
            "LIMIT :limit",
            nativeQuery = true)
    List<ItemEntity> searchItems(
            @Param("category") String category,
            @Param("keyword") String keyword,
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("radius") Double radius,
            @Param("limit") int limit
    );
}