package shoong.web_backend.domain.cart.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shoong.web_backend.domain.cart.dto.CartRequest;
import shoong.web_backend.domain.cart.dto.CartResponse;
import shoong.web_backend.domain.cart.service.CartService;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartResponse> addToCart(@RequestBody CartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(request));
    }

    @GetMapping
    public ResponseEntity<List<CartResponse>> getCartList() {
        return ResponseEntity.ok(cartService.getCartList());
    }

    @PatchMapping("/{cartId}")
    public ResponseEntity<Void> updateCartQuantity(@PathVariable Long cartId, @RequestBody CartRequest request) {
        cartService.updateCartQuantity(cartId, request.getQuantity());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable Long cartId) {
        cartService.deleteCartItem(cartId);
        return ResponseEntity.ok().build();
    }
}
