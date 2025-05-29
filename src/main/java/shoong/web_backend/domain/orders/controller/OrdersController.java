package shoong.web_backend.domain.orders.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import shoong.web_backend.domain.orders.dto.OrdersCreateRequestDto;
import shoong.web_backend.domain.orders.dto.OrdersDetailDto;
import shoong.web_backend.domain.orders.dto.OrdersResponseDto;
import shoong.web_backend.domain.orders.dto.OrdersSuccessRequestDto;
import shoong.web_backend.domain.orders.enums.OrderStatus;
import shoong.web_backend.domain.orders.service.OrdersService;
import shoong.web_backend.domain.user.dto.form.CustomUserDetails;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;

    // 주문 생성 API
    @PostMapping("/create")
    public ResponseEntity<OrdersResponseDto> createOrderDraft(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody OrdersCreateRequestDto request) {

        OrdersResponseDto orderDraft = ordersService.createOrderDraft(
                userDetails.getUserId(),
                request.getSelectedCartIds()
        );
        return ResponseEntity.ok(orderDraft);
    }

    @PostMapping("/success")
    public ResponseEntity<OrdersResponseDto> finalizeOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody OrdersSuccessRequestDto request) {

        OrdersResponseDto finalizedOrder = ordersService.finalizeOrder(
                userDetails.getUserId(),
                request.getOrderId(),
                request.getOrderAddress()
        );
        return ResponseEntity.ok(finalizedOrder);
    }

    @GetMapping("/list/paid")
    public ResponseEntity<List<OrdersDetailDto>> getPaidOrderList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<OrdersDetailDto> orderList = ordersService.findOrdersByUserIdAndOrderStatus(userDetails.getUserId(), OrderStatus.PAID);
        return ResponseEntity.ok(orderList);
    }

    @GetMapping("/list/pending")
    public ResponseEntity<List<OrdersDetailDto>> getPendingOrderList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<OrdersDetailDto> orderList = ordersService.findOrdersByUserIdAndOrderStatus(userDetails.getUserId(), OrderStatus.CREATED);
        return ResponseEntity.ok(orderList);
    }

    @GetMapping("{orderId}")
    public ResponseEntity<OrdersDetailDto> getOrderDetail(@PathVariable Long orderId) {
        OrdersDetailDto orderDetail = ordersService.findOrderDetailById(orderId);
        return ResponseEntity.ok(orderDetail);
    }
}
