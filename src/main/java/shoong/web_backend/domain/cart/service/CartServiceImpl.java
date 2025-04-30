package shoong.web_backend.domain.cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shoong.web_backend.domain.cart.dto.CartRequestDto;
import shoong.web_backend.domain.cart.dto.CartResponseDto;
import shoong.web_backend.domain.cart.entity.Cart;
import shoong.web_backend.domain.cart.repository.CartRepository;
import shoong.web_backend.domain.item.entity.Item;
import shoong.web_backend.domain.item.repository.ItemRepository;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ItemRepository itemRepository;

    @Override
    public CartResponseDto addToCart(CartRequestDto request) {
        Long userId = 1L; // 임시 로그인 유저

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found"));

        Cart cart = cartRepository.findByUserIdAndItem_ItemId(userId, request.getItemId())
                .orElseGet(() -> new Cart(user, item, 0));

        cart.updateQuantity(cart.getCartQuantity() + request.getCartQuantity());
        Cart savedCart = cartRepository.save(cart);

        return CartResponseDto.from(savedCart);
    }

    @Override
    public List<CartResponseDto> getCartList() {
        Long userId = 1L;
        List<Cart> carts = cartRepository.findAllByUserId(userId);
        return carts.stream()
                .map(CartResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public void updateCartQuantity(Long cartId, int quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        cart.updateQuantity(quantity);
        cartRepository.save(cart);
    }

    @Override
    public void deleteCartItem(Long cartId) {
        cartRepository.deleteById(cartId);
    }
}
