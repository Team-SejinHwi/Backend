package com.neo.rental.service;

import com.neo.rental.constant.ItemStatus;
import com.neo.rental.dto.ItemFormDto;
import com.neo.rental.entity.ItemEntity;
import com.neo.rental.entity.MemberEntity;
import com.neo.rental.repository.ItemRepository;
import com.neo.rental.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neo.rental.dto.ItemResponseDto; // 추가

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;

    public Long saveItem(ItemFormDto itemFormDto, String email) {

        // 1. 물건을 등록하려는 회원(주인) 찾기
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("해당 회원이 없습니다. 이메일: " + email));

        // 2. DTO -> Entity 변환 (DB에 넣기 위함)
        ItemEntity item = ItemEntity.builder()
                .title(itemFormDto.getTitle())
                .content(itemFormDto.getContent())
                .price(itemFormDto.getPrice())
                .location(itemFormDto.getLocation())
                .itemImageUrl(itemFormDto.getItemImageUrl())
                .itemStatus(ItemStatus.AVAILABLE) // 처음 등록하면 '대여 가능' 상태
                .member(member) // 주인 설정 (중요)
                .build();

        // 3. 저장
        itemRepository.save(item);

        return item.getId(); // 저장된 상품 ID 반환
    }

    // [추가 1] 상품 목록 조회 (최신순)
    @Transactional(readOnly = true) // 읽기 전용 모드 (성능 최적화)
    public List<ItemResponseDto> getItemList() {
        return itemRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ItemResponseDto::new) // Entity를 DTO로 변환
                .collect(Collectors.toList());
    }

    // [추가 2] 상품 상세 조회
    @Transactional(readOnly = true)
    public ItemResponseDto getItemDetail(Long itemId) {
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("해당 상품이 존재하지 않습니다. id=" + itemId));

        return new ItemResponseDto(item);
    }
}