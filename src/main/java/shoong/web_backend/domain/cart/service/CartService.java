package shoong.web_backend.domain.cart.service;

import shoong.web_backend.domain.cart.dto.CartRequest;
import shoong.web_backend.domain.cart.dto.CartResponse;

import java.util.List;

public interface CartService {
    CartResponse addToCart(CartRequest request);
    List<CartResponse> getCartList();
    void updateCartQuantity(Long cartId, int quantity);
    void deleteCartItem(Long cartId);
}
