package com.neo.rental.service;

import com.neo.rental.constant.ItemStatus;
import com.neo.rental.dto.ItemFormDto;
import com.neo.rental.dto.ItemResponseDto; // import 추가
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

    public Long saveItem(ItemFormDto itemFormDto, String email) {

        // 1. 물건을 등록하려는 회원(주인) 찾기
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("해당 회원이 없습니다. 이메일: " + email));

        // 2. DTO -> Entity 변환 (좌표 정보 매핑 추가)
        ItemEntity item = ItemEntity.builder()
                .title(itemFormDto.getTitle())
                .content(itemFormDto.getContent())
                .price(itemFormDto.getPrice())
                .location(itemFormDto.getLocation())
                .itemImageUrl(itemFormDto.getItemImageUrl())

                // 👇 [추가] 좌표 및 주소 매핑
                .tradeLatitude(itemFormDto.getLatitude())
                .tradeLongitude(itemFormDto.getLongitude())
                .tradeAddress(itemFormDto.getAddress())

                .itemStatus(ItemStatus.AVAILABLE)
                .member(member)
                .build();

        // 3. 저장
        itemRepository.save(item);

        return item.getId();
    }

    // 상품 목록 조회
    @Transactional(readOnly = true)
    public List<ItemResponseDto> getItemList() {
        return itemRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ItemResponseDto::new)
                .collect(Collectors.toList());
    }

    // 상품 상세 조회
    @Transactional(readOnly = true)
    public ItemResponseDto getItemDetail(Long itemId) {
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("해당 상품이 존재하지 않습니다. id=" + itemId));

        return new ItemResponseDto(item);
    }

    // [수정]
    public Long updateItem(Long itemId, ItemFormDto itemFormDto, String email) {
        // 1. 상품 조회
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));

        // 2. 주인 확인
        if (!item.getMember().getEmail().equals(email)) {
            throw new IllegalArgumentException("수정 권한이 없습니다. (본인 물건만 수정 가능)");
        }

        // 3. 이미지 URL 결정
        String targetImageUrl = itemFormDto.getItemImageUrl();
        if (targetImageUrl == null || targetImageUrl.isEmpty()) {
            targetImageUrl = item.getItemImageUrl(); // 기존 이미지 유지
        }

        // 4. 수정 진행 (좌표 정보 포함하여 업데이트)
        item.updateItem(
                itemFormDto.getTitle(),
                itemFormDto.getContent(),
                itemFormDto.getCategory(),    //[추가] 카테고리
                itemFormDto.getPrice(),
                itemFormDto.getLocation(),
                targetImageUrl,

                itemFormDto.getLatitude(),
                itemFormDto.getLongitude(),
                itemFormDto.getAddress()
        );

        return item.getId();
    }

    // [삭제]
    public void deleteItem(Long itemId, String email) {
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));

        MemberEntity requester = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("로그인된 회원 정보가 없습니다."));

        if (item.getMember() == null || !item.getMember().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("삭제 권한이 없습니다. (본인의 상품만 삭제할 수 있습니다.)");
        }

        itemRepository.delete(item);
    }
}