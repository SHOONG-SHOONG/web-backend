package shoong.web_backend.domain.order_item.entity;

import jakarta.persistence.*;
import lombok.*;
import shoong.web_backend.domain.item.entity.Item;
import shoong.web_backend.domain.orders.entity.Orders;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    // OrderItem -> Order (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private Integer orderItemQuantity;

    @Column(nullable = false)
    private Long orderItemPrice;

    public static OrderItem of(Orders order, Item item, Integer orderItemQuantity, Long orderItemPrice) {
        return OrderItem.builder()
                .order(order)
                .item(item)
                .orderItemQuantity(orderItemQuantity)
                .orderItemPrice(orderItemPrice)
                .build();
    }
}
