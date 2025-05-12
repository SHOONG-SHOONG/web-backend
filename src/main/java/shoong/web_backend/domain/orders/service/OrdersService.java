package shoong.web_backend.domain.orders.service;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webjars.NotFoundException;
import shoong.web_backend.domain.cart.entity.Cart;
import shoong.web_backend.domain.cart.repository.CartRepository;
import shoong.web_backend.domain.item.entity.Item;
import shoong.web_backend.domain.item.repository.ItemRepository;
import shoong.web_backend.domain.order_item.dto.OrderItemDto;
import shoong.web_backend.domain.order_item.entity.OrderItem;
import shoong.web_backend.domain.order_item.repository.OrderItemRepository;
import shoong.web_backend.domain.orders.dto.OrdersDetailDto;
import shoong.web_backend.domain.orders.dto.OrdersResponseDto;
import shoong.web_backend.domain.orders.entity.Orders;
import shoong.web_backend.domain.orders.repository.OrdersRepository;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.repository.UserRepository;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final RedissonClient redissonClient;

    private static final String ORDER_LOCK_PREFIX = "order:lock:";
    private static final long WAIT_TIME = 10L;
    private static final long LEASE_TIME = 5L;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    /**
     * 장바구니 상품으로 주문 생성
     */
    @Transactional
    public OrdersResponseDto saveOrderWithCartItems(Long userId) {
        User user = findUserById(userId);
        Cart cart = findUserCarts(userId);
        validateCartsNotEmpty(cart);

        Set<Long> itemIds = getItemIdsFromCart(cart);
        List<Long> sortedItemIds = itemIds.stream().sorted().toList();

        Map<Long, RLock> lockMap = new HashMap<>();


        try {
            for (Long itemId : sortedItemIds) {
                String lockKey = ORDER_LOCK_PREFIX + itemId;
                RLock lock = redissonClient.getLock(lockKey);

                boolean isLocked = lock.tryLock(WAIT_TIME, LEASE_TIME, TIME_UNIT);
                if (!isLocked) {
                    // 락 획득 실패 시 이미 획득한 락을 모두 해제
                    releaseAllLocks(lockMap);
                    throw new IllegalStateException("상품 재고 확인 중 오류가 발생했습니다. 다시 시도해주세요.");
                }

                lockMap.put(itemId, lock);
            }

            // 주문 생성 및 처리
            Orders order = createOrderFromCarts(user, cart);
            Orders savedOrder = saveOrder(order);

            List<OrderItem> orderItems = savedOrder.getOrderItems();
            orderItemRepository.saveAll(orderItems);

            // 재고 감소
            decreaseItemStock(orderItems);

            // 카트 비우기
            clearUserCart(userId);

            return convertToOrderResponseDto(savedOrder);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("주문 처리 중 인터럽트가 발생했습니다.", e);
        } finally {
            // 획득한 모든 락 해제
            releaseAllLocks(lockMap);
        }
    }

    private Set<Long> getItemIdsFromCart(Cart cart) {
        Set<Long> itemIds = new HashSet<>();

        // Cart에서 Item을 가져오는 방식에 따라 구현
        itemIds.add(cart.getItem().getItemId());

        return itemIds;
    }

    // 획득한 모든 락을 해제하는 메서드
    private void releaseAllLocks(Map<Long, RLock> lockMap) {
        for (RLock lock : lockMap.values()) {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
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

    private void decreaseItemStock(List<OrderItem> orderItems) {
        for( OrderItem orderItem : orderItems ) {
            Item item = orderItem.getItem();
            item.setItemQuantity(item.getItemQuantity() - orderItem.getOrderItemQuantity());
            itemRepository.save(item);
        }
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

    @Transactional(readOnly = true)
    public List<OrdersDetailDto> findOrdersByUserId(Long userId) {
        List<Orders> ordersList = ordersRepository.findByUserIdOrderByOrderDateDesc(userId);
        return ordersList.stream()
                .map(OrdersDetailDto::from)
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
