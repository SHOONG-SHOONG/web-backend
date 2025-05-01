package shoong.web_backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import shoong.web_backend.domain.brand.entity.Brand;
import shoong.web_backend.domain.cart.entity.Cart;
import shoong.web_backend.domain.live.entity.Live;
import shoong.web_backend.domain.orders.entity.Orders;
import shoong.web_backend.domain.user.enums.UserRole;
import shoong.web_backend.domain.user.enums.UserStatus;
import shoong.web_backend.domain.wishlist.entity.Wishlist;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;

    private String userPassword;

    private String userName;

    private String userPhone;

    private LocalDateTime birthDay;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    private String userAddress;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Orders> orders = new ArrayList<> ();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Wishlist> wishlists = new ArrayList<> ();

    @OneToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Cart> carts = new ArrayList<> ();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Live> lives = new ArrayList<> ();
}