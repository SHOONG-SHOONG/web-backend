package shoong.web_backend.domain.orders.controller;

import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shoong.web_backend.domain.orders.dto.OrdersResponseDto;
import shoong.web_backend.domain.orders.service.OrdersService;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;

    // 주문 생성 API
    @PostMapping("/success")
    public ResponseEntity<OrdersResponseDto> createOrder(@RequestParam Long userId) {
        OrdersResponseDto savedOrder = ordersService.saveOrderWithCartItems(userId);
        return ResponseEntity.ok(savedOrder);
    }
}

