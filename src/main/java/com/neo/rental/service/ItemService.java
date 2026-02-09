package com.neo.rental.service;

import com.neo.rental.constant.ItemCategory;
import com.neo.rental.constant.ItemStatus;
import com.neo.rental.constant.RentalStatus;
import com.neo.rental.dto.ItemFormDto;
import com.neo.rental.dto.ItemResponseDto;
import com.neo.rental.dto.ReviewResponseDto;
import com.neo.rental.entity.ItemEntity;
import com.neo.rental.entity.MemberEntity;
import com.neo.rental.entity.ReviewEntity;
import com.neo.rental.repository.ItemRepository;
import com.neo.rental.repository.MemberRepository;
import com.neo.rental.repository.RentalRepository;
import com.neo.rental.repository.ReviewRepository;
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
    private final ReviewRepository reviewRepository;
    private final RentalRepository rentalRepository; // ✅ 렌탈 조회를 위해 주입

    // [1. 저장]
    public Long saveItem(ItemFormDto itemFormDto, String email) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("해당 회원이 없습니다."));

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

    // [2. 검색]
    @Transactional(readOnly = true)
    public List<ItemResponseDto> searchItems(ItemCategory category, String keyword, Double lat, Double lng, Integer radiusKm, Integer limit) {
        Double radiusMeter = (radiusKm != null) ? radiusKm * 1000.0 : 5000.0;
        String categoryName = (category != null) ? category.name() : null;
        int queryLimit = (limit != null && limit > 0) ? limit : 100;

        List<ItemEntity> itemList = itemRepository.searchItems(categoryName, keyword, lat, lng, radiusMeter, queryLimit);
        return itemList.stream().map(ItemResponseDto::new).collect(Collectors.toList());
    }

    // [3. 상세 조회 - 핵심 수정]
    @Transactional(readOnly = true)
    public ItemResponseDto getItemDetail(Long itemId, String userEmail) {
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("해당 상품이 존재하지 않습니다."));

        ItemResponseDto responseDto = new ItemResponseDto(item);

        // A. 리뷰 및 평점 세팅
        List<ReviewEntity> reviewEntities = reviewRepository.findByItem_IdOrderByCreatedAtDesc(itemId);
        List<ReviewResponseDto> reviewDtos = reviewEntities.stream().map(ReviewResponseDto::new).collect(Collectors.toList());
        double averageRating = 0.0;
        if (!reviewEntities.isEmpty()) {
            averageRating = reviewEntities.stream().mapToInt(ReviewEntity::getRating).average().orElse(0.0);
            averageRating = Math.round(averageRating * 10.0) / 10.0;
        }
        responseDto.setReviews(reviewDtos);
        responseDto.setAverageRating(averageRating);
        responseDto.setReviewCount(reviewEntities.size());

        // B. 신청 여부 확인 (버튼 비활성화용)
        if (userEmail != null) {
            MemberEntity me = memberRepository.findByEmail(userEmail).orElse(null);
            if (me != null) {
                boolean exists = rentalRepository.existsByItem_IdAndRenter_IdAndStatusIn(
                        itemId, me.getId(),
                        List.of(RentalStatus.WAITING, RentalStatus.APPROVED, RentalStatus.RENTING)
                );
                responseDto.setRequested(exists);
            }
        }

        return responseDto;
    }

    // [4. 수정]
    public Long updateItem(Long itemId, ItemFormDto dto, String email) {
        ItemEntity item = itemRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("상품 없음"));
        if (!item.getMember().getEmail().equals(email)) throw new IllegalArgumentException("권한 없음");

        String targetImageUrl = dto.getItemImageUrl();
        if (targetImageUrl == null || targetImageUrl.isEmpty()) targetImageUrl = item.getItemImageUrl();

        item.updateItem(dto.getTitle(), dto.getCategory(), dto.getContent(), dto.getPrice(), dto.getLocation(), targetImageUrl, dto.getLatitude(), dto.getLongitude(), dto.getAddress());
        return item.getId();
    }

    // [5. 삭제]
    public void deleteItem(Long itemId, String email) {
        ItemEntity item = itemRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("상품 없음"));
        MemberEntity requester = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("회원 없음"));
        if (!item.getMember().getId().equals(requester.getId())) throw new IllegalArgumentException("권한 없음");
        itemRepository.delete(item);
    }
}