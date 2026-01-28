package com.neo.rental.controller;

import com.neo.rental.dto.ReviewRequestDto;
import com.neo.rental.dto.ReviewResponseDto;
import com.neo.rental.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 1. 리뷰 작성 (POST /api/reviews)
    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequestDto dto, Principal principal) {
        ReviewResponseDto result = reviewService.createReview(principal.getName(), dto);
        return ResponseEntity.ok(createResponse(true, "리뷰 작성이 완료되었습니다.", result));
    }

    // 2. 상품 리뷰 조회 (GET /api/reviews/item/{itemId})
    @GetMapping("/item/{itemId}")
    public ResponseEntity<?> getItemReviews(@PathVariable Long itemId) {
        return ResponseEntity.ok(createResponse(true, "리뷰 목록 조회 성공", reviewService.getReviewsByItem(itemId)));
    }

    // 3. 리뷰 수정
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewRequestDto requestDto,
            Principal principal) {

        ReviewResponseDto updatedReview = reviewService.updateReview(reviewId, requestDto, principal.getName());
        return ResponseEntity.ok(createResponse(true, "리뷰가 수정되었습니다.", updatedReview));
    }

    // 4. 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reviewId,
            Principal principal) {

        reviewService.deleteReview(reviewId, principal.getName());

        // 삭제된 ID만 데이터로 넘겨줌 (프론트 처리용)
        Map<String, Object> data = new HashMap<>();
        data.put("reviewId", reviewId);
        data.put("status", "DELETED");

        return ResponseEntity.ok(createResponse(true, "리뷰가 삭제되었습니다.", data));
    }

    // ==========================================
    // 유틸리티 메서드 (JSON 응답 생성용) - 통일됨
    // ==========================================
    private Map<String, Object> createResponse(boolean success, String message, Object data) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success", success);
        res.put("code", success ? 200 : 400);
        res.put("message", message);
        if (data != null) {
            res.put("data", data);
        }
        return res;
    }
}