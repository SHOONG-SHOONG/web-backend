package shoong.web_backend.domain.order_item.entity;

import jakarta.persistence.*;
import lombok.*;
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

    @Column(nullable = false)
    private Long itemId; // 상품 아이디

    @Column(nullable = false)
    private Integer orderItemQuantity;

    @Column(nullable = false)
    private Long orderItemPrice;

    public static OrderItem of(Orders order, Long itemId, Integer orderItemQuantity, Long orderItemPrice) {
        return OrderItem.builder()
                .order(order)
                .itemId(itemId)
                .orderItemQuantity(orderItemQuantity)
                .orderItemPrice(orderItemPrice)
                .build();
    }
}
