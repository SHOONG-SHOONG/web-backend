package shoong.web_backend.domain.cart.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shoong.web_backend.domain.cart.dto.CartRequestDto;
import shoong.web_backend.domain.cart.dto.CartResponseDto;
import shoong.web_backend.domain.cart.service.CartService;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    //api/cart: 장바구니 담기
    @PostMapping
    public ResponseEntity<CartResponseDto> addToCart(@RequestBody CartRequestDto request) {
        return ResponseEntity.ok(cartService.addToCart(request));
    }

    //장바구니 조회
    @GetMapping
    public ResponseEntity<List<CartResponseDto>> getCartList() {
        return ResponseEntity.ok(cartService.getCartList());
    }

    //장바구니 담긴 아이템 수량 변경
    @PatchMapping("/{cartId}")
    public ResponseEntity<Void> updateCartQuantity(@PathVariable Long cartId, @RequestBody CartRequestDto request) {
        cartService.updateCartQuantity(cartId, request.getCartQuantity());
        return ResponseEntity.ok().build();
    }

    //장바구니 물품 삭제
    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable Long cartId) {
        cartService.deleteCartItem(cartId);
        return ResponseEntity.ok().build();
    }
}
