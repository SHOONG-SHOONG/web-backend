package shoong.web_backend.domain.orders.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shoong.web_backend.domain.orders.dto.OrdersResponseDto;
import shoong.web_backend.domain.orders.service.OrdersService;
import shoong.web_backend.domain.user.dto.form.CustomUserDetails;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;

    // 주문 생성 API
    @PostMapping("/success")
    public ResponseEntity<OrdersResponseDto> createOrder(@AuthenticationPrincipal CustomUserDetails userDetails) {
        OrdersResponseDto savedOrder = ordersService.saveOrderWithCartItems(userDetails.getUserId());
        return ResponseEntity.ok(savedOrder);
    }
}
