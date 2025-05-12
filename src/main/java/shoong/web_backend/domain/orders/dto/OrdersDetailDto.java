package shoong.web_backend.domain.orders.dto;

import lombok.Builder;
import lombok.Getter;
import shoong.web_backend.domain.order_item.dto.OrderItemDetailDto;
import shoong.web_backend.domain.orders.entity.Orders;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrdersDetailDto {
    private Long orderId;
    private Long totalPrice;
    private LocalDateTime orderDate;
    private String orderAddress;
    private List<OrderItemDetailDto> orderItems;

    public static OrdersDetailDto from(Orders order) {
        return OrdersDetailDto.builder()
                .orderId(order.getOrderId())
                .totalPrice(order.getTotalPrice())
                .orderDate(order.getOrderDate())
                .orderAddress(order.getOrderAddress())
                .orderItems(order.getOrderItems().stream()
                        .map(OrderItemDetailDto::from)
                        .toList())
                .build();
    }
}