package com.neo.rental.controller;

import com.neo.rental.dto.ItemResponseDto; // 추가
import com.neo.rental.dto.ItemFormDto;
import com.neo.rental.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<?> createItem(@RequestBody ItemFormDto itemFormDto/*, Principal principal*/) {

        // 1. 로그인 체크
//        if (principal == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
//        }

        // 2. 서비스 호출 (등록)
//        String email = principal.getName(); // 로그인한 사람의 이메일
        String email = "test@gmail.com";
        try {
            Long savedItemId = itemService.saveItem(itemFormDto, email);
            return ResponseEntity.ok().body("상품 등록 완료! ID: " + savedItemId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("등록 실패: " + e.getMessage());
        }
    }
    // [추가 1] 상품 목록 조회
    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> getItemList() {
        List<ItemResponseDto> items = itemService.getItemList();
        return ResponseEntity.ok(items);
    }

    // [추가 2] 상품 상세 조회
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> getItemDetail(@PathVariable Long itemId) {
        ItemResponseDto item = itemService.getItemDetail(itemId);
        return ResponseEntity.ok(item);
    }

    // [수정]
    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateItem(@PathVariable Long itemId,
                                        @RequestBody ItemFormDto itemFormDto
                                        /*Principal principal*/) { // Principal: 로그인 정보

        // 테스트용 (Principal이 null일 때 임시 처리 : 추후 삭제)
//        String email = (principal != null) ? principal.getName() : "test@gmail.com";
        String email = "test@gmail.com";

        try {
            itemService.updateItem(itemId, itemFormDto, email);
            return ResponseEntity.ok("상품 수정 완료! ID: " + itemId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // [삭제]
    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteItem(@PathVariable Long itemId/*, Principal principal*/) {

//        String email = (principal != null) ? principal.getName() : "test@test.com";
        String email = "test@gmail.com";

        try {
            itemService.deleteItem(itemId, email);
            return ResponseEntity.ok("상품 삭제 완료! ID: " + itemId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}