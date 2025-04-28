package shoong.web_backend.domain.order_item.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import shoong.web_backend.domain.orders.entity.Orders;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    // OrderItem -> Order (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    private Long itemId; // 상품 아이디

    @Column(nullable = false)
    private Integer orderItemQuantity;

    @Column(nullable = false)
    private Integer orderItemPrice;

}
