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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import shoong.web_backend.exception.NotFoundException;
import org.slf4j.MDC;
import shoong.web_backend.domain.live.enums.LiveStatus;
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
            validateStockAvailabilityWithOrder(orderItems);

            decreaseItemStock(orderItems);
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
            String timestamp = Instant.now().toString();

            String userAgeStr = String.valueOf(Period.between(user.getBirthDay(), LocalDate.now()).getYears());
            int userAge = Period.between(user.getBirthDay(), LocalDate.now()).getYears();
            // ✅ 연령대 매핑
            String ageGroup;
            if (userAge >= 10 && userAge < 20) {
                ageGroup = "10-19";
            } else if (userAge < 30) {
                ageGroup = "20-29";
            } else if (userAge < 40) {
                ageGroup = "30-39";
            } else if (userAge < 50) {
                ageGroup = "40-49";
            } else if (userAge < 60) {
                ageGroup = "50-59";
            } else if (userAge < 70) {
                ageGroup = "60-69";
            } else if (userAge < 80) {
                ageGroup = "70-79";
            } else {
                ageGroup = "80+";
            }

            for (OrderItem orderItem : orderItems) {
                Long itemId = orderItem.getItem().getItemId();
                LiveItem matchedLiveItem = liveItems.stream()
                        .filter(li -> li.getItem().getItemId().equals(itemId))
                        .findFirst()
                        .orElse(null);

                if (matchedLiveItem != null) {
                    Long liveId = matchedLiveItem.getLive().getId();
                    String liveStartTime = matchedLiveItem.getLive().getLiveStartTime().toString();
                    String liveEndTime = matchedLiveItem.getLive().getLiveEndTime().toString();

                    MDC.put("eventType", eventType);
                    MDC.put("userId", userIdStr);
                    MDC.put("orderId", orderIdStr);
                    MDC.put("userAge", userAgeStr);
                    MDC.put("ageGroup", ageGroup);
                    MDC.put("timestamp", timestamp);
                    MDC.put("itemId", String.valueOf(itemId));
                    MDC.put("quantity", String.valueOf(orderItem.getOrderItemQuantity()));
                    MDC.put("price", String.valueOf(orderItem.getItem().getPrice() * orderItem.getOrderItemQuantity() * (1 - orderItem.getItem().getDiscountRate())));
                    MDC.put("liveId", String.valueOf(liveId));
                    MDC.put("liveStartTime", liveStartTime);
                    MDC.put("liveEndTime", liveEndTime);

                    log.info("결제 완료 이벤트 발생");
                    MDC.clear();
                }
            }

            for (OrderItem orderItem : orderItems) {
                Item item = orderItem.getItem();
                Long itemId = item.getItemId();

                // live 정보 가져오기
                LiveItem matchedLiveItem = liveItems.stream()
                        .filter(li -> li.getItem().getItemId().equals(itemId))
                        .findFirst()
                        .orElse(null);

                if (matchedLiveItem != null) {
                    String liveId = String.valueOf(matchedLiveItem.getLive().getId());
                    String liveStartTime = matchedLiveItem.getLive().getLiveStartTime().toString();
                    String liveEndTime = matchedLiveItem.getLive().getLiveEndTime().toString();

                    MDC.put("liveId", liveId);
                    MDC.put("liveStartTime", liveStartTime);
                    MDC.put("liveEndTime", liveEndTime);
                }

                MDC.put("eventType", "purchase");
                MDC.put("userId", String.valueOf(user.getId()));
                MDC.put("userAge", String.valueOf(Period.between(user.getBirthDay(), LocalDate.now()).getYears()));
                MDC.put("orderId", String.valueOf(order.getOrderId()));
                MDC.put("itemId", String.valueOf(item.getItemId()));
                MDC.put("itemName", item.getItemName());
                MDC.put("category", item.getCategory());
                MDC.put("quantity", String.valueOf(orderItem.getOrderItemQuantity()));
                MDC.put("price", String.valueOf(item.getPrice()));
                MDC.put("totalPrice", String.valueOf(item.getPrice() * orderItem.getOrderItemQuantity()));
                MDC.put("timestamp", Instant.now().toString());

                MDC.put("생일: {}", String.valueOf(user.getBirthDay()));
                MDC.put("오늘 날짜: {}", String.valueOf(LocalDate.now()));

                log.info("상품 구매 완료");
                MDC.clear();
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
        double raw = cart.getItem().getPrice() *
                cart.getCartQuantity() *
                (1 - cart.getItem().getDiscountRate());

        return Math.round(raw);
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
