package shoong.web_backend.domain.wishlist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shoong.web_backend.domain.orders.dto.OrdersResponseDto;
import shoong.web_backend.domain.wishlist.dto.WishlistResponseDTO;
import shoong.web_backend.domain.wishlist.service.WishlistService;

import java.util.List;


@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    // 찜 생성 API
    @PostMapping("/{userId}/{itemId}")
    public ResponseEntity<String> addWishlistItem(@PathVariable Long userId, @PathVariable Long itemId) {
        // 찜을 누르면 회원id와 상품id를 insert
        wishlistService.addWishlistItem(userId, itemId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Wishlist item created successfully");
    }

    // 찜 목록 조회
    @GetMapping("/{userId}")
    public ResponseEntity<List<WishlistResponseDTO>> getWishlist(@PathVariable Long userId) {
        List<WishlistResponseDTO> wishlist = wishlistService.getWishlistByUserId(userId);
        return ResponseEntity.ok(wishlist);
    }

    // 찜 삭제
    @DeleteMapping("/{wishlistId}")
    public ResponseEntity<Void> deleteWishlistItem(@PathVariable Long wishlistId) {
        wishlistService.deleteWishlistItem(wishlistId);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
}
