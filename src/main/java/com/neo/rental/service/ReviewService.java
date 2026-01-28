package com.neo.rental.service;

import com.neo.rental.dto.ReviewRequestDto;
import com.neo.rental.dto.ReviewResponseDto;
import com.neo.rental.entity.*;
import com.neo.rental.repository.*;
import com.neo.rental.constant.RentalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RentalRepository rentalRepository;
    private final MemberRepository memberRepository;

    // 1. 리뷰 작성
    public ReviewResponseDto createReview(String email, ReviewRequestDto dto) {
        MemberEntity reviewer = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보 없음"));

        RentalEntity rental = rentalRepository.findById(dto.getRentalId())
                .orElseThrow(() -> new IllegalArgumentException("대여 정보 없음"));

        // [검증 1] 작성자가 실제 대여한 사람(Renter)인지 확인
        if (!rental.getRenter().getId().equals(reviewer.getId())) {
            throw new IllegalStateException("본인이 이용한 거래만 리뷰를 작성할 수 있습니다.");
        }

        // [검증 2] 이미 리뷰를 작성했는지 확인
        if (reviewRepository.findByRental_Id(dto.getRentalId()).isPresent()) {
            throw new IllegalStateException("이미 리뷰를 작성한 거래입니다.");
        }

        // [검증 3] (선택사항) 거래가 완료(APPROVED 등) 상태인지 확인
        // if (rental.getStatus() != RentalStatus.APPROVED) { ... }

        ReviewEntity review = ReviewEntity.builder()
                .rental(rental)
                .item(rental.getItem())
                .reviewer(reviewer)
                .rating(dto.getRating())
                .content(dto.getContent())
                .build();

        return new ReviewResponseDto(reviewRepository.save(review));
    }

    // 2. 상품별 리뷰 조회
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsByItem(Long itemId) {
        return reviewRepository.findByItem_IdOrderByCreatedAtDesc(itemId).stream()
                .map(ReviewResponseDto::new)
                .collect(Collectors.toList());
    }
}