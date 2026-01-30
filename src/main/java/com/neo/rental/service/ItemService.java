package com.neo.rental.service;

import com.neo.rental.constant.ItemCategory;
import com.neo.rental.constant.ItemStatus;
import com.neo.rental.dto.ItemFormDto;
import com.neo.rental.dto.ItemResponseDto;
import com.neo.rental.entity.ItemEntity;
import com.neo.rental.entity.MemberEntity;
import com.neo.rental.repository.ItemRepository;
import com.neo.rental.repository.MemberRepository;
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

    // [수정] 상품 목록 검색 (List 반환, 페이징 제거)
    @Transactional(readOnly = true)
    public List<ItemResponseDto> searchItems(
            ItemCategory category,
            String keyword,
            Double lat,
            Double lng,
            Integer radiusKm) {

        // 1. 반경 계산 (기본 5km)
        Double radiusMeter = (radiusKm != null) ? radiusKm * 1000.0 : 5000.0;

        // 2. 카테고리 Enum -> String 변환
        String categoryName = (category != null) ? category.name() : null;

        // 3. 레포지토리 호출 (List 반환)
        List<ItemEntity> itemList = itemRepository.searchItems(
                categoryName,
                keyword,
                lat,
                lng,
                radiusMeter
        );

        // 4. Entity List -> DTO List 변환
        return itemList.stream()
                .map(ItemResponseDto::new) // DTO 생성자 사용
                .collect(Collectors.toList());
    }

    // [기존] 상세 조회 (유지)
    @Transactional(readOnly = true)
    public ItemResponseDto getItemDetail(Long itemId) {
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("해당 상품이 존재하지 않습니다. id=" + itemId));
        return new ItemResponseDto(item);
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