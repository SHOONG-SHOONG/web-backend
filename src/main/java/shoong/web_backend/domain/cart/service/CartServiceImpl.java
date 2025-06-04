package shoong.web_backend.domain.cart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import shoong.web_backend.domain.cart.dto.CartRequestDto;
import shoong.web_backend.domain.cart.dto.CartResponseDto;
import shoong.web_backend.domain.cart.entity.Cart;
import shoong.web_backend.domain.cart.repository.CartRepository;
import shoong.web_backend.domain.item.entity.Item;
import shoong.web_backend.domain.item.repository.ItemRepository;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.repository.UserRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ItemRepository itemRepository;

    @Override
    public CartResponseDto addToCart(CartRequestDto request, User user) {

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if(request.getCartQuantity() > item.getItemQuantity()) {
            throw new IllegalStateException("재고 수량을 초과했습니다.");
        }

        Cart cart = cartRepository.findByUserIdAndItem_ItemId(user.getId(), request.getItemId())
                .orElseGet(() -> new Cart(user, item, 0));

        if (cart.getCartQuantity() + request.getCartQuantity() > item.getItemQuantity()) {
            throw new IllegalStateException("재고 수량을 초과했습니다.");
        }

        cart.updateQuantity(cart.getCartQuantity() + request.getCartQuantity());
        Cart savedCart = cartRepository.save(cart);

        MDC.put("eventType", "cart_added");
        MDC.put("timestamp", Instant.now().toString());

        // 유저 관련
        MDC.put("userId", String.valueOf(user.getId()));
        MDC.put("userAge", String.valueOf(Period.between(user.getBirthDay(), LocalDate.now()).getYears()));

        // item 관련
        MDC.put("itemId",  String.valueOf(item.getItemId()));
        MDC.put("itemName",  String.valueOf(item.getItemName()));
        MDC.put("category",  String.valueOf(item.getCategory()));
        MDC.put("itemId",  String.valueOf(item.getPrice()));


        log.info("장바구니 추가 이벤트 발생");
        MDC.clear();

        return CartResponseDto.from(savedCart);
    }

    @Override
    public List<CartResponseDto> getCartList(User user) {

        List<Cart> carts = cartRepository.findAllByUserId(user.getId());

        return carts.stream()
                .map(CartResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public void updateCartQuantity(Long cartId, int quantity, Long userId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (!cart.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("해당 장바구니에 접근 권한이 없습니다.");
        }

        if (quantity > cart.getItem().getItemQuantity()) {
            throw new IllegalStateException("재고 수량을 초과했습니다.");
        }

        cart.updateQuantity(quantity);
        cartRepository.save(cart);
    }

    @Override
    public void deleteCartItem(Long cartId) {
        cartRepository.deleteById(cartId);
    }
}
