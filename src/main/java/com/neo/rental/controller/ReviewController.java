package com.neo.rental.controller;

import com.neo.rental.dto.ReviewRequestDto;
import com.neo.rental.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 공통 응답 헬퍼
    private Map<String, Object> createResponse(Object data, String msg) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success", true);
        res.put("code", 200);
        res.put("message", msg);
        if (data != null) res.put("data", data);
        return res;
    }

    // 1. 리뷰 작성 (POST /api/reviews)
    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequestDto dto, Principal principal) {
        return ResponseEntity.ok(createResponse(
                reviewService.createReview(principal.getName(), dto),
                "리뷰 작성이 완료되었습니다."
        ));
    }

    // 2. 상품 리뷰 조회 (GET /api/reviews/item/{itemId})
    @GetMapping("/item/{itemId}")
    public ResponseEntity<?> getItemReviews(@PathVariable Long itemId) {
        return ResponseEntity.ok(createResponse(
                reviewService.getReviewsByItem(itemId),
                "리뷰 목록 조회 성공"
        ));
    }
}