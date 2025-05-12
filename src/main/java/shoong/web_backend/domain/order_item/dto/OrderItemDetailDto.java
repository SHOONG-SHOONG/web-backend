package shoong.web_backend.domain.order_item.dto;

import lombok.Builder;
import lombok.Getter;
import shoong.web_backend.domain.order_item.entity.OrderItem;

@Getter
@Builder
public class OrderItemDetailDto {
    private Long orderItemId;
    private Long orderId;
    private Long itemId;
    private String itemName;
    private Integer quantity;
    private Long price;

    public static OrderItemDetailDto from(OrderItem orderItem) {
        return OrderItemDetailDto.builder()
                .orderItemId(orderItem.getOrderItemId())
                .orderId(orderItem.getOrder().getOrderId())
                .itemId(orderItem.getItem().getItemId())
                .itemName(orderItem.getItem().getItemName())
                .quantity(orderItem.getOrderItemQuantity())
                .price(orderItem.getOrderItemPrice())
                .build();
    }
}