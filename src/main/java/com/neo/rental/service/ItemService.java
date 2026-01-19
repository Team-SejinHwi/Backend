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
}