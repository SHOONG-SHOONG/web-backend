package shoong.web_backend.domain.cart.entity;

import jakarta.persistence.*;
import lombok.*;
import shoong.web_backend.domain.item.entity.Item;
import shoong.web_backend.domain.user.entity.User;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;

    // Cart -> Item (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Integer cartQuantity;

    public Cart(User user, Item item, Integer cartQuantity) {
        this.user = user;
        this.item = item;
        this.cartQuantity = cartQuantity;
    }

    public void updateQuantity(int cartQuantity) {
        this.cartQuantity = cartQuantity;
    }
}
