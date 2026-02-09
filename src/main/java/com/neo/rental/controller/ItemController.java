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

    // 2. [수정됨] 상품 목록 조회 (Limit 적용 완료)
    // 메인화면용(8개): GET /api/items?limit=8
    // 검색용(기본 300개): GET /api/items?keyword=맥북
    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> searchItems(
            @RequestParam(required = false) ItemCategory category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Integer radius,
            @RequestParam(required = false) Integer limit // 👈 [추가] 파라미터 수신
    ) {
        // Service 호출 (limit 값 전달)
        List<ItemResponseDto> items = itemService.searchItems(
                category, keyword, lat, lng, radius, limit
        );

        return ResponseEntity.ok(items);
    }

    // ✅ [3. 상세 조회 (수정됨)]
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> getItemDetail(@PathVariable Long itemId, Principal principal) {
        String email = (principal != null) ? principal.getName() : null;
        return ResponseEntity.ok(itemService.getItemDetail(itemId, email));
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