package shoong.web_backend.domain.item.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import jakarta.persistence.*;
import lombok.*;
import shoong.web_backend.domain.brand.entity.Brand;
import shoong.web_backend.domain.cart.entity.Cart;
import shoong.web_backend.domain.item.enums.ItemStatus;
import shoong.web_backend.domain.item_image.entity.ItemImage;
import shoong.web_backend.domain.live_item.entity.LiveItem;
import shoong.web_backend.domain.order_item.entity.OrderItem;
import shoong.web_backend.domain.wishlist.entity.Wishlist;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<Wishlist> wishlists = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<Cart> carts = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<LiveItem> liveItems = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<ItemImage> itemImages = new ArrayList<>();

    private String itemName;

    private Long price;

    private Double discountRate;

    private String description;

    private Integer itemQuantity;

    private String category;

    private LocalDateTime createdAt;

    private LocalDateTime discountExpiredAt;

    @Enumerated(EnumType.STRING)
    private ItemStatus status;
}
