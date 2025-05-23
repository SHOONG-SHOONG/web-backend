package shoong.web_backend.domain.orders.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import shoong.web_backend.domain.orders.dto.OrderCreateRequestDto;
import shoong.web_backend.domain.orders.dto.OrdersDetailDto;
import shoong.web_backend.domain.orders.dto.OrdersResponseDto;
import shoong.web_backend.domain.orders.service.OrdersService;
import shoong.web_backend.domain.user.dto.form.CustomUserDetails;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;

    // 주문 생성 API
    @PostMapping("/success")
    public ResponseEntity<OrdersResponseDto> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody OrderCreateRequestDto request) {

        OrdersResponseDto savedOrder = ordersService.saveOrderWithSelectedCartItems(
                userDetails.getUserId(),
                request.getSelectedCartIds()
        );
        return ResponseEntity.ok(savedOrder);
    }

    @GetMapping("/list")
    public ResponseEntity<List<OrdersDetailDto>> getOrderList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<OrdersDetailDto> orderList = ordersService.findOrdersByUserId(userDetails.getUserId());
        return ResponseEntity.ok(orderList);
    }
}
