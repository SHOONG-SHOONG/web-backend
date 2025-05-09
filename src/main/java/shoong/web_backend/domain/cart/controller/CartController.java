package shoong.web_backend.domain.cart.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import shoong.web_backend.domain.cart.dto.CartRequestDto;
import shoong.web_backend.domain.cart.dto.CartResponseDto;
import shoong.web_backend.domain.cart.service.CartService;
import shoong.web_backend.domain.user.dto.form.CustomUserDetails;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    @Operation(summary = "장바구니 추가", description = "장바구니 추가 api")
    @PostMapping("/add")
    public ResponseEntity<CartResponseDto> addToCart(@RequestBody CartRequestDto request,
                                                     @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        User user = userRepository.findById(customUserDetails.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 유저가 존재하지 않습니다."));

        return ResponseEntity.ok(cartService.addToCart(request, user));
    }

    @Operation(summary = "장바구니 조회", description = "장바구니 조회 api")
    @GetMapping("/get")
    public ResponseEntity<List<CartResponseDto>> getCartList(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        User user = userRepository.findById(customUserDetails.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 유저가 존재하지 않습니다."));
        return ResponseEntity.ok(cartService.getCartList(user));
    }

    @Operation(summary = "장바구니 수량 수정", description = "장바구니 수정 api")
    @PatchMapping("/change/{cartId}")
    public ResponseEntity<Void> updateCartQuantity(@PathVariable Long cartId, @RequestParam int quantity, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        cartService.updateCartQuantity(cartId, quantity, customUserDetails.getUserId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "장바구니 삭제", description = "장바구니 삭제 api")
    @DeleteMapping("/delete/{cartId}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable Long cartId) {
        cartService.deleteCartItem(cartId);
        return ResponseEntity.ok().build();
    }
}
