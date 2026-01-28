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

import com.neo.rental.dto.ItemResponseDto;

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

        // 2. DTO -> Entity 변환
        ItemEntity item = ItemEntity.builder()
                .title(itemFormDto.getTitle())
                .content(itemFormDto.getContent())
                .price(itemFormDto.getPrice())
                .location(itemFormDto.getLocation())
                .itemImageUrl(itemFormDto.getItemImageUrl())
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

    // [수정] ★ 이미지가 없으면 기존 이미지 유지 로직 추가됨
    public Long updateItem(Long itemId, ItemFormDto itemFormDto, String email) {
        // 1. 상품 조회
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));

        // 2. 주인 확인
        if (!item.getMember().getEmail().equals(email)) {
            throw new IllegalArgumentException("수정 권한이 없습니다. (본인 물건만 수정 가능)");
        }

        // 3. 이미지 URL 결정 로직 (핵심)
        // DTO에 새 이미지 URL이 있으면 그걸 쓰고, 없으면(null) 기존 DB에 있던 URL(item.getItemImageUrl())을 쓴다.
        String targetImageUrl = itemFormDto.getItemImageUrl();
        if (targetImageUrl == null || targetImageUrl.isEmpty()) {
            targetImageUrl = item.getItemImageUrl(); // 기존 이미지 유지
        }

        // 4. 수정 진행 (결정된 이미지 URL을 전달)
        item.updateItem(
                itemFormDto.getTitle(),
                itemFormDto.getContent(),
                itemFormDto.getPrice(),
                itemFormDto.getLocation(),
                targetImageUrl // <--- 여기가 중요!
        );

        // (참고) 만약 상태(Status)도 수정해야 한다면 별도로 setter 호출 필요
        // item.setItemStatus(itemFormDto.getItemStatus());

        return item.getId();
    }

    // [삭제] - ID 비교 방식으로 강화
    public void deleteItem(Long itemId, String email) {
        // 1. 삭제할 상품 조회
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));

        // 2. 요청자(현재 로그인한 사람)의 ID 조회
        //    (String 비교보다는 DB에 있는 확실한 ID 값을 가져와서 비교하는 게 가장 안전함)
        MemberEntity requester = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("로그인된 회원 정보가 없습니다."));

        // 3. 주인 확인 (ID 비교)
        //    item.getMember()가 null이 아니고,
        //    상품 주인의 ID와 요청자의 ID가 다르면 예외 발생
        if (item.getMember() == null || !item.getMember().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("삭제 권한이 없습니다. (본인의 상품만 삭제할 수 있습니다.)");
        }

        // 4. 삭제 진행
        itemRepository.delete(item);
    }
}