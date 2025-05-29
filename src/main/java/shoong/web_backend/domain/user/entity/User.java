package shoong.web_backend.domain.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import shoong.web_backend.domain.brand.entity.Brand;
import shoong.web_backend.domain.cart.entity.Cart;
import shoong.web_backend.domain.live.entity.Live;
import shoong.web_backend.domain.orders.entity.Orders;
import shoong.web_backend.domain.user.enums.UserRole;
import shoong.web_backend.domain.user.enums.UserStatus;
import shoong.web_backend.domain.wishlist.entity.Wishlist;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "user",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_email", "user_name", "user_alias"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //
    @Size(max = 10, message = "별명은 최대 10자까지 입력할 수 있습니다.")
    private String userAlias;

    @Email(message = "유효한 이메일 형식이어야 합니다.")
    @NotBlank(message = "이메일은 필수 항목입니다.")
    @Size(max = 100, message = "이메일은 최대 100자까지 입력할 수 있습니다.")
    private String userEmail;

    @NotBlank(message = "비밀번호는 필수 항목입니다.")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하로 입력해주세요.")
    private String userPassword;

    @NotBlank(message = "ID는 필수 항목입니다.")
    @Size(max = 50, message = "userName은 최대 50자까지 입력할 수 있습니다.")
    private String userName;

    @Size(max = 50, message = "이름은 최대 50자까지 입력할 수 있습니다.")
    private String name;

    @Size(max = 20, message = "전화번호는 최대 20자까지 입력할 수 있습니다.")
    private String userPhone;

    private LocalDate birthDay;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    @Size(max = 200, message = "주소는 최대 200자까지 입력할 수 있습니다.")
    private String userAddress;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Orders> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Wishlist> wishlists = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Cart> carts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Live> lives = new ArrayList<>();

//    @Builder
//    public User(String userName, String userPassword, UserRole role){
//        this.userName = userName;
//        this.userPassword = userPassword;
//        this.role = role;
//    }
}

