package shoong.web_backend.domain.orders.service;

import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import shoong.web_backend.domain.cart.entity.Cart;
import shoong.web_backend.domain.cart.repository.CartRepository;
import shoong.web_backend.domain.order_item.dto.OrderItemDto;
import shoong.web_backend.domain.order_item.entity.OrderItem;
import shoong.web_backend.domain.orders.dto.OrdersResponseDto;
import shoong.web_backend.domain.orders.entity.Orders;
import shoong.web_backend.domain.orders.repository.OrdersRepository;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    /**
     * 장바구니 상품으로 주문 생성
     */
    @Transactional
    public OrdersResponseDto saveOrderWithCartItems(Long userId) {
        User user = findUserById(userId);
        Cart cart = findUserCarts(userId);
        validateCartsNotEmpty(cart);

        Orders order = createOrderFromCarts(user, cart);
        Orders savedOrder = saveOrder(order);
        // order가 완료 되었으므로 카트 비우기
        clearUserCart(userId);

        return convertToOrderResponseDto(savedOrder);
    }

    // ===== 사용자 관련 헬퍼 메서드 =====
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 userID의 유저 조회를 실패하였습니다."));
    }

    // ===== 장바구니 관련 헬퍼 메서드 =====
    private Cart findUserCarts(Long userId) {
        return cartRepository.findTopByUserIdOrderByCartIdDesc(userId);
    }

    private void validateCartsNotEmpty(Cart cart) {
        if (cart == null) {
            throw new IllegalStateException("장바구니가 비어있습니다.");
        }
    }

    private void clearUserCart(Long userId) {
        cartRepository.deleteAllByUserId(userId);
    }

    // ===== 주문 관련 헬퍼 메서드 =====
    private Orders createOrderFromCarts(User user,Cart carts) {
        Orders order = Orders.of(user);
        OrderItem orderItem = createOrderItemFromCart(order, carts);
        order.addOrderItem(orderItem);
        return order;
    }

    private OrderItem createOrderItemFromCart(Orders order, Cart cart) {
        return OrderItem.of(
                order,
                cart.getItem(),
                cart.getCartQuantity(),
                calculateOrderItemPrice(cart)
        );
    }

    private Long calculateOrderItemPrice(Cart cart) {
        return (long) (cart.getItem().getPrice() *
                cart.getCartQuantity() *
                cart.getItem().getDiscountRate());
    }

    private Orders saveOrder(Orders order) {
        return ordersRepository.save(order);
    }

    // ===== DTO 변환 헬퍼 메서드 =====
    private OrdersResponseDto convertToOrderResponseDto(Orders savedOrder) {
        return OrdersResponseDto.builder()
                .orderId(savedOrder.getOrderId())
                .totalPrice(savedOrder.getTotalPrice())
                .orderDate(savedOrder.getOrderDate())
                .orderItems(convertToOrderItemDtoList(savedOrder.getOrderItems()))
                .build();
    }

    private List<OrderItemDto> convertToOrderItemDtoList(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(OrderItemDto::from)
                .toList();
    }
}



//@Service
//@RequiredArgsConstructor
//@Builder
//public class OrdersService {
//
//    private final OrdersRepository ordersRepository;
//    private final CartRepository cartRepository;
//    private final UserRepository userRepository;
//
//    @Transactional
//    public OrdersResponseDto saveOrderWithCartItems(Long userId) {
//        // 1. 유저 장바구니 조회
//        List<Cart> carts = cartRepository.findAllByUserId(userId);
//        User getUser = userRepository.findById(userId)
//                .orElseThrow(() -> new NotFoundException("해당 userID의 유저 조회를 실패하였습니다."));
//
//        if (carts.isEmpty()) {
//            throw new IllegalStateException("장바구니가 비어있습니다.");
//        }
//
//        Orders order = Orders.of(getUser);
//
//        // 3. 장바구니 -> OrderItem 변환해서 Orders에 추가
//        for (Cart cart : carts) {
//            OrderItem orderItem = OrderItem.of(
//                    order,
//                    cart.getItem(),
//                    cart.getCartQuantity(),
//                    (long) (cart.getItem().getPrice() * cart.getCartQuantity() * cart.getItem().getDiscountRate())
//                    // 상품 가격 기준
//            );
//            order.addOrderItem(orderItem);
//        }
//
//        // 4. Orders 저장 (OrderItem은 cascade로 같이 저장됨)
//        Orders savedOrder = ordersRepository.save(order);
//
//        // 5. 장바구니 비우기
//        cartRepository.deleteAllByUserId(userId);
//
//        return OrdersResponseDto.builder()
//                .orderId(savedOrder.getOrderId())
//                .totalPrice(savedOrder.getTotalPrice())
//                .orderDate(savedOrder.getOrderDate())
//                .orderItems(savedOrder.getOrderItems().stream()
//                        .map(OrderItemDto::from)
//                        .toList())
//                .build();
//    }
//}
