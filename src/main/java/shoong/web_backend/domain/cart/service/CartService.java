package shoong.web_backend.domain.cart.service;

import shoong.web_backend.domain.cart.dto.CartRequestDto;
import shoong.web_backend.domain.cart.dto.CartResponseDto;
import shoong.web_backend.domain.user.entity.User;

import java.util.List;

public interface CartService {
    CartResponseDto addToCart(CartRequestDto request, User user);
    List<CartResponseDto> getCartList(User user);
    void updateCartQuantity(Long cartId, int cartQuantity, Long userId);
    void deleteCartItem(Long cartId);
}
