package shoong.web_backend.domain.cart.service;

import shoong.web_backend.domain.cart.dto.CartRequestDto;
import shoong.web_backend.domain.cart.dto.CartResponseDto;

import java.util.List;

public interface CartService {
    CartResponseDto addToCart(CartRequestDto request);
    List<CartResponseDto> getCartList();
    void updateCartQuantity(Long cartId, int cartQuantity);
    void deleteCartItem(Long cartId);
}
