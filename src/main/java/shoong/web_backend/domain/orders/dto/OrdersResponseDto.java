package shoong.web_backend.domain.orders.dto;

import lombok.Builder;
import lombok.Getter;
import shoong.web_backend.domain.order_item.dto.OrderItemDto;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrdersResponseDto {
    private Long orderId;
    private Long totalPrice;
    private LocalDateTime orderDate;
    private List<OrderItemDto> orderItems;
}
