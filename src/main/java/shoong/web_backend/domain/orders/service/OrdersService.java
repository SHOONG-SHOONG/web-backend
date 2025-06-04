package shoong.web_backend.domain.orders.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shoong.web_backend.domain.cart.entity.Cart;
import shoong.web_backend.domain.cart.repository.CartRepository;
import shoong.web_backend.domain.item.entity.Item;
import shoong.web_backend.domain.item.repository.ItemRepository;
import shoong.web_backend.domain.order_item.dto.OrderItemDetailDto;
import shoong.web_backend.domain.order_item.entity.OrderItem;
import shoong.web_backend.domain.order_item.repository.OrderItemRepository;
import shoong.web_backend.domain.orders.dto.OrdersDetailDto;
import shoong.web_backend.domain.orders.entity.Orders;
import shoong.web_backend.domain.orders.enums.OrderStatus;
import shoong.web_backend.domain.orders.repository.OrdersRepository;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.repository.UserRepository;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import shoong.web_backend.exception.NotFoundException;
import org.slf4j.MDC;
import shoong.web_backend.domain.live.enums.LiveStatus;
import shoong.web_backend.domain.live.repository.LiveRepository;
import shoong.web_backend.domain.live_item.entity.LiveItem;
import shoong.web_backend.domain.live_item.repository.LiveItemRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final RedissonClient redissonClient;
    private final LiveRepository liveRepository;

    private static final String ORDER_LOCK_PREFIX = "order:lock:";
    private static final long WAIT_TIME = 10L;
    private static final long LEASE_TIME = 5L;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    private final LiveItemRepository liveItemRepository;

    /**
     * 선택된 장바구니 상품으로 주문 생성
     * @param userId 사용자 ID
     * @param selectedCartIds 주문할 장바구니 아이템 ID 목록
     */
    @Transactional
    public OrdersDetailDto createOrderDraft(Long userId, List<Long> selectedCartIds) {
        User user = findUserById(userId);
        List<Cart> selectedCarts = findSelectedCarts(userId, selectedCartIds);
        validateCartsNotEmpty(selectedCarts);

        // 선택된 카트에서 아이템 ID 추출 및 정렬 (데드락 방지)
        Set<Long> itemIds = getItemIdsFromCarts(selectedCarts);
        List<Long> sortedItemIds = itemIds.stream().sorted().toList();

        Map<Long, RLock> lockMap = new HashMap<>();

        try {
            // 아이템별 락 획득
            acquireItemLocks(sortedItemIds, lockMap);

            // 재고 검증
            validateStockAvailabilityWithCart(selectedCarts);

            // 주문 생성 및 처리
            Orders order = createOrderFromSelectedCarts(user, selectedCarts);
            Orders savedOrder = saveOrder(order);

            List<OrderItem> orderItems = savedOrder.getOrderItems();
            orderItemRepository.saveAll(orderItems);

            return convertToOrderResponseDto(savedOrder);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("주문 처리 중 인터럽트가 발생했습니다.", e);
        } finally {
            // 획득한 모든 락 해제
            releaseAllLocks(lockMap);
        }
    }

    @Transactional
    public OrdersDetailDto finalizeOrder(Long userId, Long orderId, String orderAddress) {
        User user = findUserById(userId);
        Orders order = findOrderById(orderId);
//        validateUserOwnership(order, userId);

        List<OrderItem> orderItems = order.getOrderItems();

        List<Long> itemIds = orderItems.stream()
                .map(oi -> oi.getItem().getItemId())
                .distinct()
                .sorted()
                .toList();

        Map<Long, RLock> lockMap = new HashMap<>();
        try {
            acquireItemLocks(itemIds, lockMap);
            validateStockAvailabilityWithOrder(orderItems); // ✅ 재확인 안전망

            decreaseItemStock(orderItems); // ✅ 실제 차감
            removeSelectedCartItems(userId, order);
            order.setOrderAddress(orderAddress);
            order.setOrderStatus(OrderStatus.PAID);

            List<LiveItem> liveItems = liveItemRepository.findOngoingLiveItems(itemIds, LiveStatus.ONGOING);

            // itemId → liveId 매핑
            Map<Long, Long> itemIdToLiveId = liveItems.stream()
                    .collect(Collectors.toMap(
                            li -> li.getItem().getItemId(),
                            li -> li.getLive().getId(),
                            (existing, replacement) -> existing
                    ));

            String eventType = "order_finalized";
            String userIdStr = String.valueOf(userId);
            String orderIdStr = String.valueOf(orderId);
            String userAgeStr = String.valueOf(Period.between(user.getBirthDay(), LocalDate.now()).getYears());
            String timestamp = Instant.now().toString();
            for (OrderItem orderItem : orderItems) {
                Long itemId = orderItem.getItem().getItemId();
                Long liveId = itemIdToLiveId.get(itemId);

                if (liveId != null) {
                    MDC.put("eventType", eventType);
                    MDC.put("userId", userIdStr);
                    MDC.put("orderId", orderIdStr);
                    MDC.put("userAge", userAgeStr);
                    MDC.put("timestamp", timestamp);
                    MDC.put("itemId", String.valueOf(itemId));
                    MDC.put("quantity", String.valueOf(orderItem.getOrderItemQuantity()));
                    MDC.put("price", String.valueOf(orderItem.getItem().getPrice()*orderItem.getOrderItemQuantity()*(1-orderItem.getItem().getDiscountRate())));
                    MDC.put("liveId", String.valueOf(liveId));

                    log.info("결제 완료 이벤트 발생 (아이템 단위)");
                    MDC.clear();
                }
            }

            return convertToOrderResponseDto(order);
        } catch (InterruptedException e) {
            order.setOrderStatus(OrderStatus.FAILED);
            Thread.currentThread().interrupt();
            throw new RuntimeException("주문 확정 중 인터럽트 발생", e);
        } finally {
            releaseAllLocks(lockMap);
            MDC.clear();
        }
    }

    @Transactional(readOnly = true)
    public List<OrdersDetailDto> findOrdersByUserIdAndOrderStatus(Long userId, OrderStatus orderStatus) {
        List<Orders> ordersList = ordersRepository.findByUserIdAndOrderStatus(userId, orderStatus)
                .stream()
                .toList();

        return ordersList.stream()
                .map(OrdersDetailDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrdersDetailDto findOrderDetailById(Long orderId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("해당 orderId의 주문 조회를 실패했습니다."));

        return OrdersDetailDto.from(order);
    }

    private Orders findOrderById(Long orderId) {
        return ordersRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("해당 orderId의 주문 조회를 실패했습니다."));
    }

//    private void validateUserOwnership(Orders order, Long userId) {
//        if (!order.getUser().getId().equals(userId)) {
//            throw new UnauthorizedAccessException("해당 주문에 접근할 권한이 없습니다.");
//        }
//    }

    public String getAgeGroup(int age) {
        if (age >= 10 && age < 20) {
            return "10대";
        } else if (age >= 20 && age < 30) {
            return "20대";
        } else if (age >= 30 && age < 40) {
            return "30대";
        } else if (age >= 40 && age < 50) {
            return "40대";
        } else if (age >= 50 && age < 60) {
            return "50대";
        } else if (age >= 60 && age < 70) {
            return "60대";
        } else if (age >= 70 && age < 80) {
            return "70대";
        } else {
            return "80대 이상";
        }
    }

    // ===== 락 관련 헬퍼 메서드 =====
    private void acquireItemLocks(List<Long> sortedItemIds, Map<Long, RLock> lockMap)
            throws InterruptedException {
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
    }

    private void releaseAllLocks(Map<Long, RLock> lockMap) {
        for (RLock lock : lockMap.values()) {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // ===== 재고 검증 헬퍼 메서드 =====
    private void validateStockAvailabilityWithCart(List<Cart> selectedCarts) {
        for (Cart cart : selectedCarts) {
            Item item = cart.getItem();
            int requestedQuantity = cart.getCartQuantity();
            int availableStock = item.getItemQuantity();

            if (requestedQuantity > availableStock) {
                throw new IllegalStateException(
                        String.format("상품 '%s'의 재고가 부족합니다. (요청: %d개, 재고: %d개)",
                                item.getItemName(), requestedQuantity, availableStock)
                );
            }
        }
    }

    private void validateStockAvailabilityWithOrder(List<OrderItem> orderItems) {
        for (OrderItem orderItem : orderItems) {
            Item item = orderItem.getItem();
            int requestedQuantity = orderItem.getOrderItemQuantity();
            int availableStock = item.getItemQuantity();

            if (requestedQuantity > availableStock) {
                throw new IllegalStateException(
                        String.format("상품 '%s'의 재고가 부족합니다. (요청: %d개, 재고: %d개)",
                                item.getItemName(), requestedQuantity, availableStock)
                );
            }
        }
    }

    // ===== 장바구니 관련 헬퍼 메서드 =====
    private List<Cart> findSelectedCarts(Long userId, List<Long> selectedCartIds) {
        List<Cart> carts = cartRepository.findByUserIdAndCartIdIn(userId, selectedCartIds);

        // 요청된 카트 ID와 실제 조회된 카트 ID 비교
        if (carts.size() != selectedCartIds.size()) {
            throw new NotFoundException("일부 장바구니 항목을 찾을 수 없습니다.");
        }

        return carts;
    }

    private void validateCartsNotEmpty(List<Cart> carts) {
        if (carts == null || carts.isEmpty()) {
            throw new IllegalStateException("장바구니가 비어있습니다.");
        }
    }

    private Set<Long> getItemIdsFromCarts(List<Cart> carts) {
        return carts.stream()
                .map(cart -> cart.getItem().getItemId())
                .collect(Collectors.toSet());
    }

    private void removeSelectedCartItems(Long userId, Orders order) {
        // 주문에서 사용된 itemId들 추출
        Set<Long> orderedItemIds = order.getOrderItems().stream()
                .map(oi -> oi.getItem().getItemId())
                .collect(Collectors.toSet());

        // 내 카트 목록 조회
        List<Cart> myCarts = cartRepository.findByUserId(userId);

        // 주문된 itemId와 일치하는 카트만 필터링
        List<Cart> cartsToRemove = myCarts.stream()
                .filter(cart -> orderedItemIds.contains(cart.getItem().getItemId()))
                .toList();

        cartRepository.deleteAll(cartsToRemove);
    }

    // ===== 사용자 관련 헬퍼 메서드 =====
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 userID의 유저 조회를 실패하였습니다."));
    }

    // ===== 주문 관련 헬퍼 메서드 =====
    private Orders createOrderFromSelectedCarts(User user, List<Cart> selectedCarts) {
        // 기존의 CREATED 상태 주문이 있다면 삭제
        ordersRepository.findByUserIdAndOrderStatus(user.getId(), OrderStatus.CREATED)
                .ifPresent(ordersRepository::delete);  // cascade 설정이 되어있다면 OrderItem도 함께 삭제됨

        // 새로운 주문 생성
        Orders order = Orders.of(user);

        for (Cart cart : selectedCarts) {
            OrderItem orderItem = createOrderItemFromCart(order, cart);
            order.addOrderItem(orderItem);
        }

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
                (1 - cart.getItem().getDiscountRate()));
    }

    private Orders saveOrder(Orders order) {
        return ordersRepository.save(order);
    }

    private void decreaseItemStock(List<OrderItem> orderItems) {
        for (OrderItem orderItem : orderItems) {
            Item item = orderItem.getItem();
            int newQuantity = item.getItemQuantity() - orderItem.getOrderItemQuantity();

            if (newQuantity < 0) {
                throw new IllegalStateException(
                        String.format("상품 '%s'의 재고가 부족합니다.", item.getItemName())
                );
            }

            item.setItemQuantity(newQuantity);
            itemRepository.save(item);
        }
    }

    // ===== DTO 변환 헬퍼 메서드 =====
    private OrdersDetailDto convertToOrderResponseDto(Orders savedOrder) {
        return OrdersDetailDto.builder()
                .orderId(savedOrder.getOrderId())
                .totalPrice(savedOrder.getTotalPrice())
                .orderDate(savedOrder.getOrderDate())
                .orderItems(convertToOrderItemDtoList(savedOrder.getOrderItems()))
                .orderAddress(savedOrder.getOrderAddress())
                .build();
    }

    private List<OrderItemDetailDto> convertToOrderItemDtoList(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(OrderItemDetailDto::from)
                .toList();
    }
}
