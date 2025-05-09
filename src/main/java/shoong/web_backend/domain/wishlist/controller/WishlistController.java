package shoong.web_backend.domain.wishlist.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import shoong.web_backend.domain.orders.dto.OrdersResponseDto;
import shoong.web_backend.domain.user.dto.form.CustomUserDetails;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.repository.UserRepository;
import shoong.web_backend.domain.wishlist.dto.WishlistResponseDTO;
import shoong.web_backend.domain.wishlist.service.WishlistService;

import java.util.List;


@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserRepository userRepository;

    @Operation(summary = "찜 생성 or 찜 삭제", description = "찜이 되어있을 시 취소, 찜이 안 되어있을 시 찜 생성")
    @PostMapping("/toggle/{itemId}")
    public ResponseEntity<String> addWishlistItem(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                  @PathVariable Long itemId) {

        User user = userRepository.findById(customUserDetails.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 유저가 존재하지 않습니다."));

        boolean liked = wishlistService.toggleWishlist(user, itemId);
        return ResponseEntity.ok(liked ? "Liked" : "Unliked");
    }

    @Operation(summary = "찜 목록 조회")
    @GetMapping("/get")
    public ResponseEntity<List<WishlistResponseDTO>> getWishlist(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        User user = userRepository.findById(customUserDetails.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 유저가 존재하지 않습니다."));

        List<WishlistResponseDTO> wishlist = wishlistService.getWishlist(user);
        return ResponseEntity.ok(wishlist);
    }
}
