package shoong.web_backend.domain.item.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import shoong.web_backend.domain.item.condition.ItemSearchCondition;
import shoong.web_backend.domain.item.dto.ItemRequestDto;
import shoong.web_backend.domain.item.dto.ItemResponseDto;
import shoong.web_backend.domain.item.service.ItemService;
import shoong.web_backend.domain.user.dto.form.CustomUserDetails;

@RestController
@RequestMapping("/item")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // 상품 등록
    // CRUD: Post, URI: /item
    @PostMapping
    public ResponseEntity<Void> createItem(@RequestBody @Valid ItemRequestDto requestDto,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        itemService.createItem(requestDto, userDetails.getUserId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    // 상품 상세 조회
    // CRUD: Get, URI: /item/{itemId}
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> getItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(itemService.getItem(itemId));
    }

    //    // 카테고리별 상품 조회
//    // CRUD: Get, URI: /item/category/{categoryName}
//    @GetMapping("/category/{categoryName}")
//    public ResponseEntity<List<ItemResponseDto>> getItemsByCategory(@PathVariable Long itemId){
//        return ResponseEntity.ok(itemService.getItem(itemId));
//    }
    // QueryDSL 을 이용한 동적 쿼리 부분 -> 조건에 따른 상품 조회
    @GetMapping("/search")
    public ResponseEntity<Page<ItemResponseDto>> searchItems(
            ItemSearchCondition condition,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<ItemResponseDto> result = itemService.searchItems(condition, pageable);
        return ResponseEntity.ok(result);
    }

    //상품 삭제
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.ok().build();
    }
}
