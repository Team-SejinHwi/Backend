package com.neo.rental.controller;

import com.neo.rental.constant.ItemCategory;
import com.neo.rental.dto.ItemResponseDto;
import com.neo.rental.dto.ItemFormDto;
import com.neo.rental.service.FileService;
import com.neo.rental.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final FileService fileService;

    // 1. 상품 등록 (유지)
    @PostMapping
    public ResponseEntity<?> createItem(
            @RequestPart(value = "itemData") ItemFormDto itemFormDto,
            @RequestPart(value = "itemImage", required = false) MultipartFile itemImage,
            Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인 필요"));

        try {
            String imageUrl = null;
            if (itemImage != null && !itemImage.isEmpty()) imageUrl = fileService.uploadFile(itemImage);
            itemFormDto.setItemImageUrl(imageUrl);

            Long savedItemId = itemService.saveItem(itemFormDto, principal.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "상품 등록 완료");
            response.put("itemId", savedItemId);
            response.put("imageUrl", imageUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", "등록 실패", "error", e.getMessage()));
        }
    }

    // 2. [수정됨] 상품 목록 조회 (List 반환, 페이징 X)
    // 요청 예시: /api/items (전체 최신순)
    // 요청 예시: /api/items?lat=37.5&lng=127.0&radius=5 (내 주변 5km)
    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> searchItems(
            @RequestParam(required = false) ItemCategory category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Integer radius
    ) {
        // Service 호출 (단순 List 반환)
        List<ItemResponseDto> items = itemService.searchItems(
                category, keyword, lat, lng, radius
        );

        // 깔끔하게 배열([]) 형태로 반환
        return ResponseEntity.ok(items);
    }

    // 3. 상세 조회 (유지)
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> getItemDetail(@PathVariable Long itemId) {
        ItemResponseDto item = itemService.getItemDetail(itemId);
        return ResponseEntity.ok(item);
    }

    // 4. 수정 (유지)
    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateItem(@PathVariable Long itemId,
                                        @RequestPart(value = "itemData") ItemFormDto itemFormDto,
                                        @RequestPart(value = "itemImage", required = false) MultipartFile itemImage,
                                        Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인 필요"));

        try {
            if (itemImage != null && !itemImage.isEmpty()) {
                String imageUrl = fileService.uploadFile(itemImage);
                itemFormDto.setItemImageUrl(imageUrl);
            }
            itemService.updateItem(itemId, itemFormDto, principal.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "상품 수정 완료");
            response.put("itemId", itemId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "수정 실패", "error", e.getMessage()));
        }
    }

    // 5. 삭제 (유지)
    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteItem(@PathVariable Long itemId, Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인 필요"));

        try {
            itemService.deleteItem(itemId, principal.getName());
            return ResponseEntity.ok(Map.of("message", "상품 삭제 완료", "itemId", itemId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "삭제 실패", "error", e.getMessage()));
        }
    }
}