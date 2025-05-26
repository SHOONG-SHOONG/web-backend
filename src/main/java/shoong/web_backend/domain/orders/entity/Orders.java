package shoong.web_backend.domain.orders.entity;

import jakarta.persistence.*;
import lombok.*;
import shoong.web_backend.domain.order_item.entity.OrderItem;
import shoong.web_backend.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(nullable = false)
    private Long totalPrice;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    private String orderAddress;

    // Orders -> OrderItem (1:N)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public static Orders of(User user) {
        return Orders.builder()
                .user(user)
                .orderAddress(user.getUserAddress())
                .totalPrice(0L)
                .orderDate(LocalDateTime.now())
                .build();
    }

    // 편의 메소드
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
        totalPrice = 0L;
        totalPrice += orderItem.getOrderItemPrice();
    }
}
