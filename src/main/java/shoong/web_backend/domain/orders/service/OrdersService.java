package shoong.web_backend.domain.orders.service;

import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shoong.web_backend.domain.cart.entity.Cart;
import shoong.web_backend.domain.cart.repository.CartRepository;
import shoong.web_backend.domain.order_item.dto.OrderItemDto;
import shoong.web_backend.domain.order_item.entity.OrderItem;
import shoong.web_backend.domain.orders.dto.OrdersResponseDto;
import shoong.web_backend.domain.orders.entity.Orders;
import shoong.web_backend.domain.orders.repository.OrdersRepository;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.enums.UserRole;

import java.util.List;

@Service
@RequiredArgsConstructor
@Builder
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final CartRepository cartRepository;

    @Transactional
    public OrdersResponseDto saveOrderWithCartItems(User user) {
        long userId = user.getId();


        // 1. 유저 장바구니 조회
        List<Cart> carts = cartRepository.findAllByUserId(userId);
        if (carts.isEmpty()) {
            throw new IllegalStateException("장바구니가 비어있습니다.");
        }

        // 2. Orders 객체 생성
        // mock User 생성
        User mockUser = User.builder()
                .id(userId)
                .userEmail("test@client.com")
                .userName("테스트구매자")
                .role(UserRole.CLIENT)
                .build();
        Orders order = Orders.of(mockUser);

        // 3. 장바구니 -> OrderItem 변환해서 Orders에 추가
        for (Cart cart : carts) {
            OrderItem orderItem = OrderItem.of(
                    order,
                    cart.getItem(),
                    cart.getCartQuantity(),
                    cart.getItem().getPrice() * cart.getCartQuantity()   // 상품 가격 기준
            );
            order.addOrderItem(orderItem);
        }

        // 4. Orders 저장 (OrderItem은 cascade로 같이 저장됨)
        Orders savedOrder = ordersRepository.save(order);

        // 5. 장바구니 비우기
        cartRepository.deleteAllByUserId(userId);

        return OrdersResponseDto.builder()
                .orderId(savedOrder.getOrderId())
                .totalPrice(savedOrder.getTotalPrice())
                .orderDate(savedOrder.getOrderDate())
                .orderItems(savedOrder.getOrderItems().stream()
                        .map(OrderItemDto::from)
                        .toList())
                .build();
    }
}
