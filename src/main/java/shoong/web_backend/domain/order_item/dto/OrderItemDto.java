package shoong.web_backend.domain.order_item.dto;

import lombok.Builder;
import lombok.Getter;
import shoong.web_backend.domain.order_item.entity.OrderItem;

@Getter
@Builder
public class OrderItemDto {
    private Long itemId;
    private String itemName;
    private Integer quantity;
    private Long price;

    public static OrderItemDto from(OrderItem orderItem) {
        return OrderItemDto.builder()
                .itemId(orderItem.getItem().getItemId())
                .itemName(orderItem.getItem().getItemName())
                .quantity(orderItem.getOrderItemQuantity())
                .price(orderItem.getOrderItemPrice())
                .build();
    }
}
