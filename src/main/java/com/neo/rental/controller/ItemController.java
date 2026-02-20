package com.neo.rental.controller;

import com.neo.rental.constant.ItemCategory;
import com.neo.rental.dto.ItemResponseDto;
import com.neo.rental.dto.ItemFormDto;
import com.neo.rental.service.S3Service; // FileService 대신 S3Service 로 변경
import com.neo.rental.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final S3Service s3Service; // s3 사용을 위해 교체

    // 1. 상품 등록
    @PostMapping
    public ResponseEntity<?> createItem(
            @RequestPart(value = "itemData") ItemFormDto itemFormDto,
            @RequestPart(value = "itemImage", required = false) MultipartFile itemImage,
            Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인 필요"));

        try {
            String imageUrl = null;
            // 로컬 폴더 대신 S3에 업로드하고 URL을 받아옵니다.
            if (itemImage != null && !itemImage.isEmpty()) imageUrl = s3Service.uploadImage(itemImage);
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

    // 2. 상품 목록 조회
    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> searchItems(
            @RequestParam(required = false) ItemCategory category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Integer radius,
            @RequestParam(required = false) Integer limit
    ) {
        List<ItemResponseDto> items = itemService.searchItems(
                category, keyword, lat, lng, radius, limit
        );
        return ResponseEntity.ok(items);
    }

    // 3. [상세 조회] - 로그인 유저 정보 전달
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> getItemDetail(
            @PathVariable Long itemId,
            Principal principal // 로그인 안 했으면 null
    ) {
        String email = (principal != null) ? principal.getName() : null;
        return ResponseEntity.ok(itemService.getItemDetail(itemId, email));
    }

    // 4. 수정
    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateItem(@PathVariable Long itemId,
                                        @RequestPart(value = "itemData") ItemFormDto itemFormDto,
                                        @RequestPart(value = "itemImage", required = false) MultipartFile itemImage,
                                        Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인 필요"));

        try {
            if (itemImage != null && !itemImage.isEmpty()) {
                // 수정할 때도 S3에 업로드
                String imageUrl = s3Service.uploadImage(itemImage);
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

    // 5. 삭제
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