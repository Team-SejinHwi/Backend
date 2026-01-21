package com.neo.rental.controller;

import com.neo.rental.dto.ItemResponseDto;
import com.neo.rental.dto.ItemFormDto;
import com.neo.rental.service.FileService;
import com.neo.rental.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // 필수

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final FileService fileService; // [추가] 파일 저장 서비스

    @PostMapping
    // [중요] consumes 설정 추가 (이게 없으면 415 또 뜰 수 있음)
    public ResponseEntity<?> createItem(
            @RequestPart(value = "itemData") ItemFormDto itemFormDto, // JSON 데이터
            @RequestPart(value = "itemImage", required = false) MultipartFile itemImage, // 이미지 파일
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            // 1. 이미지 파일 저장 및 경로 획득
            String imageUrl = null;
            if (itemImage != null && !itemImage.isEmpty()) {
                imageUrl = fileService.uploadFile(itemImage);
            }

            // 2. DTO에 이미지 경로 세팅 (DTO에 setter 필요)
            itemFormDto.setItemImageUrl(imageUrl);

            // 3. 서비스 호출 (기존 로직 그대로 사용)
            Long savedItemId = itemService.saveItem(itemFormDto, principal.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "상품 등록 완료");
            response.put("itemId", savedItemId);
            response.put("imageUrl", imageUrl); // 확인용

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // 서버 로그에 에러 찍기
            Map<String, String> error = new HashMap<>();
            error.put("message", "등록 실패");
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 2. 상품 목록 조회 (List는 자동으로 JSON 배열 []로 변환되므로 수정 불필요)
    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> getItemList() {
        List<ItemResponseDto> items = itemService.getItemList();
        return ResponseEntity.ok(items);
    }

    // 3. 상품 상세 조회 (DTO는 자동으로 JSON 객체 {}로 변환되므로 수정 불필요)
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> getItemDetail(@PathVariable Long itemId) {
        ItemResponseDto item = itemService.getItemDetail(itemId);
        return ResponseEntity.ok(item);
    }

    // 4. [수정] JSON 반환으로 변경
    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateItem(@PathVariable Long itemId,
                                        @RequestBody ItemFormDto itemFormDto,
                                        Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            itemService.updateItem(itemId, itemFormDto, principal.getName());

            // [변경 포인트] String -> Map (JSON)
            Map<String, Object> response = new HashMap<>();
            response.put("message", "상품 수정 완료");
            response.put("itemId", itemId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "수정 실패");
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 5. [삭제] JSON 반환으로 변경
    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteItem(@PathVariable Long itemId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            itemService.deleteItem(itemId, principal.getName());

            // [변경 포인트] String -> Map (JSON)
            Map<String, Object> response = new HashMap<>();
            response.put("message", "상품 삭제 완료");
            response.put("itemId", itemId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "삭제 실패");
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}