package com.neo.rental.controller;

import com.neo.rental.dto.ItemFormDto;
import com.neo.rental.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<?> createItem(@RequestBody ItemFormDto itemFormDto, Principal principal) {

        // 1. 로그인 체크
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        // 2. 서비스 호출 (등록)
        String email = principal.getName(); // 로그인한 사람의 이메일
        try {
            Long savedItemId = itemService.saveItem(itemFormDto, email);
            return ResponseEntity.ok().body("상품 등록 완료! ID: " + savedItemId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("등록 실패: " + e.getMessage());
        }
    }
}