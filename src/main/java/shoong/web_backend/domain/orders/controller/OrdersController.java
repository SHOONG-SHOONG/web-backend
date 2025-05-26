package shoong.web_backend.domain.orders.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import shoong.web_backend.domain.orders.dto.OrdersCreateRequestDto;
import shoong.web_backend.domain.orders.dto.OrdersDetailDto;
import shoong.web_backend.domain.orders.dto.OrdersResponseDto;
import shoong.web_backend.domain.orders.dto.OrdersSuccessRequestDto;
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
                request.getOrderId()
        );
        return ResponseEntity.ok(finalizedOrder);
    }

    @GetMapping("/list")
    public ResponseEntity<List<OrdersDetailDto>> getOrderList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<OrdersDetailDto> orderList = ordersService.findOrdersByUserId(userDetails.getUserId());
        return ResponseEntity.ok(orderList);
    }
}
