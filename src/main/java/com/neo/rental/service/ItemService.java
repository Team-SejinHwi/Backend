package com.neo.rental.service;

import com.neo.rental.constant.ItemCategory;
import com.neo.rental.constant.ItemStatus;
import com.neo.rental.dto.ItemFormDto;
import com.neo.rental.dto.ItemResponseDto;
import com.neo.rental.dto.ReviewResponseDto; // [추가]
import com.neo.rental.entity.ItemEntity;
import com.neo.rental.entity.MemberEntity;
import com.neo.rental.entity.ReviewEntity; // [추가]
import com.neo.rental.repository.ItemRepository;
import com.neo.rental.repository.MemberRepository;
import com.neo.rental.repository.ReviewRepository; // [추가]
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository; // 👈 [추가] 리뷰 조회를 위해 주입

    // [기존] 저장 로직 (유지)
    public Long saveItem(ItemFormDto itemFormDto, String email) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("해당 회원이 없습니다. 이메일: " + email));

        ItemEntity item = ItemEntity.builder()
                .title(itemFormDto.getTitle())
                .category(itemFormDto.getCategory())
                .content(itemFormDto.getContent())
                .price(itemFormDto.getPrice())
                .location(itemFormDto.getLocation())
                .itemImageUrl(itemFormDto.getItemImageUrl())
                .tradeLatitude(itemFormDto.getLatitude())
                .tradeLongitude(itemFormDto.getLongitude())
                .tradeAddress(itemFormDto.getAddress())
                .itemStatus(ItemStatus.AVAILABLE)
                .member(member)
                .build();

        itemRepository.save(item);
        return item.getId();
    }

    // [기존] 상품 목록 검색 (유지)
    @Transactional(readOnly = true)
    public List<ItemResponseDto> searchItems(
            ItemCategory category,
            String keyword,
            Double lat,
            Double lng,
            Integer radiusKm,
            Integer limit) {

        Double radiusMeter = (radiusKm != null) ? radiusKm * 1000.0 : 5000.0;
        String categoryName = (category != null) ? category.name() : null;
        int queryLimit = (limit != null && limit > 0) ? limit : 100;

        List<ItemEntity> itemList = itemRepository.searchItems(
                categoryName,
                keyword,
                lat,
                lng,
                radiusMeter,
                queryLimit
        );

        return itemList.stream()
                .map(ItemResponseDto::new)
                .collect(Collectors.toList());
    }

    // ✅ [수정됨] 상세 조회 (리뷰 + 평점 포함)
    @Transactional(readOnly = true)
    public ItemResponseDto getItemDetail(Long itemId) {
        // 1. 상품 조회
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("해당 상품이 존재하지 않습니다. id=" + itemId));

        // 2. 기본 DTO 생성 (Item 정보만 있음)
        ItemResponseDto responseDto = new ItemResponseDto(item);

        // 3. [추가] 리뷰 목록 조회 (최신순)
        List<ReviewEntity> reviewEntities = reviewRepository.findByItem_IdOrderByCreatedAtDesc(itemId);

        // 4. [추가] 리뷰 DTO 리스트로 변환
        List<ReviewResponseDto> reviewDtos = reviewEntities.stream()
                .map(ReviewResponseDto::new)
                .collect(Collectors.toList());

        // 5. [추가] 평균 별점 계산
        double averageRating = 0.0;
        if (!reviewEntities.isEmpty()) {
            averageRating = reviewEntities.stream()
                    .mapToInt(ReviewEntity::getRating)
                    .average()
                    .orElse(0.0);

            // 소수점 한 자리 반올림 (예: 4.333 -> 4.3)
            averageRating = Math.round(averageRating * 10.0) / 10.0;
        }

        // 6. [추가] DTO에 리뷰 정보 세팅
        responseDto.setReviews(reviewDtos);
        responseDto.setAverageRating(averageRating);
        responseDto.setReviewCount(reviewEntities.size());

        return responseDto;
    }

    // [기존] 수정 로직 (유지)
    public Long updateItem(Long itemId, ItemFormDto itemFormDto, String email) {
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));

        if (!item.getMember().getEmail().equals(email)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        String targetImageUrl = itemFormDto.getItemImageUrl();
        if (targetImageUrl == null || targetImageUrl.isEmpty()) {
            targetImageUrl = item.getItemImageUrl();
        }

        item.updateItem(
                itemFormDto.getTitle(),
                itemFormDto.getCategory(),
                itemFormDto.getContent(),
                itemFormDto.getPrice(),
                itemFormDto.getLocation(),
                targetImageUrl,
                itemFormDto.getLatitude(),
                itemFormDto.getLongitude(),
                itemFormDto.getAddress()
        );
        return item.getId();
    }

    // [기존] 삭제 로직 (유지)
    public void deleteItem(Long itemId, String email) {
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));
        MemberEntity requester = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보가 없습니다."));

        if (item.getMember() == null || !item.getMember().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }
        itemRepository.delete(item);
    }
}