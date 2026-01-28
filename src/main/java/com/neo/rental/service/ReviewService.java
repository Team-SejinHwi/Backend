package com.neo.rental.service;

import com.neo.rental.dto.ReviewRequestDto;
import com.neo.rental.dto.ReviewResponseDto;
import com.neo.rental.entity.*;
import com.neo.rental.repository.*;
import com.neo.rental.constant.RentalStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

        // [검증 3] 거래가 완료된 상태인지 확인 (중요!)
        // 상태가 RETURNED(반납됨) 상태일 때만 리뷰 가능하도록 설정
        if (rental.getStatus() != RentalStatus.RETURNED) {
            throw new IllegalStateException("반납이 완료된 대여 건에 대해서만 리뷰를 작성할 수 있습니다.");
        }

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

    // 3. 리뷰 수정 (기간 제한 로직 포함)
    public ReviewResponseDto updateReview(Long reviewId, ReviewRequestDto requestDto, String email) {
        // 1. 리뷰 조회
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 리뷰입니다."));

        // 2. 작성자 본인 확인
        if (!review.getReviewer().getEmail().equals(email)) {
            throw new IllegalStateException("본인의 리뷰만 수정할 수 있습니다.");
        }

        // 3. [핵심] 수정 가능 기간 체크 (대여 종료 후 3일 이내)
        // review -> rental -> endDate를 가져옴
        LocalDateTime rentalEndDate = review.getRental().getEndDate();
        LocalDateTime deadline = rentalEndDate.plusDays(3); // 마감기한 = 종료일 + 3일

        if (LocalDateTime.now().isAfter(deadline)) {
            throw new IllegalStateException("리뷰 수정 기간이 지났습니다. (대여 종료 후 3일 이내만 가능)");
        }

        // 4. 수정 진행
        review.updateReview(requestDto.getRating(), requestDto.getContent());

        return new ReviewResponseDto(review);
    }

    // 4. 리뷰 삭제
    public void deleteReview(Long reviewId, String email) {
        // 1. 리뷰 조회
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 리뷰입니다."));

        // 2. 권한 검증
        if (!review.getReviewer().getEmail().equals(email)) {
            throw new IllegalStateException("본인의 리뷰만 삭제할 수 있습니다.");
        }

        // 3. 삭제
        reviewRepository.delete(review);
    }
}