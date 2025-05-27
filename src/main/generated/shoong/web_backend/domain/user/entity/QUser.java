package shoong.web_backend.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 217222296L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUser user = new QUser("user");

    public final DatePath<java.time.LocalDate> birthDay = createDate("birthDay", java.time.LocalDate.class);

    public final shoong.web_backend.domain.brand.entity.QBrand brand;

    public final ListPath<shoong.web_backend.domain.cart.entity.Cart, shoong.web_backend.domain.cart.entity.QCart> carts = this.<shoong.web_backend.domain.cart.entity.Cart, shoong.web_backend.domain.cart.entity.QCart>createList("carts", shoong.web_backend.domain.cart.entity.Cart.class, shoong.web_backend.domain.cart.entity.QCart.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<shoong.web_backend.domain.live.entity.Live, shoong.web_backend.domain.live.entity.QLive> lives = this.<shoong.web_backend.domain.live.entity.Live, shoong.web_backend.domain.live.entity.QLive>createList("lives", shoong.web_backend.domain.live.entity.Live.class, shoong.web_backend.domain.live.entity.QLive.class, PathInits.DIRECT2);

    public final StringPath name = createString("name");

    public final ListPath<shoong.web_backend.domain.orders.entity.Orders, shoong.web_backend.domain.orders.entity.QOrders> orders = this.<shoong.web_backend.domain.orders.entity.Orders, shoong.web_backend.domain.orders.entity.QOrders>createList("orders", shoong.web_backend.domain.orders.entity.Orders.class, shoong.web_backend.domain.orders.entity.QOrders.class, PathInits.DIRECT2);

    public final StringPath registrationNumber = createString("registrationNumber");

    public final EnumPath<shoong.web_backend.domain.user.enums.UserRole> role = createEnum("role", shoong.web_backend.domain.user.enums.UserRole.class);

    public final StringPath userAddress = createString("userAddress");

    public final StringPath userAlias = createString("userAlias");

    public final StringPath userEmail = createString("userEmail");

    public final StringPath userName = createString("userName");

    public final StringPath userPassword = createString("userPassword");

    public final StringPath userPhone = createString("userPhone");

    public final EnumPath<shoong.web_backend.domain.user.enums.UserStatus> userStatus = createEnum("userStatus", shoong.web_backend.domain.user.enums.UserStatus.class);

    public final ListPath<shoong.web_backend.domain.wishlist.entity.Wishlist, shoong.web_backend.domain.wishlist.entity.QWishlist> wishlists = this.<shoong.web_backend.domain.wishlist.entity.Wishlist, shoong.web_backend.domain.wishlist.entity.QWishlist>createList("wishlists", shoong.web_backend.domain.wishlist.entity.Wishlist.class, shoong.web_backend.domain.wishlist.entity.QWishlist.class, PathInits.DIRECT2);

    public QUser(String variable) {
        this(User.class, forVariable(variable), INITS);
    }

    public QUser(Path<? extends User> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUser(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUser(PathMetadata metadata, PathInits inits) {
        this(User.class, metadata, inits);
    }

    public QUser(Class<? extends User> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.brand = inits.isInitialized("brand") ? new shoong.web_backend.domain.brand.entity.QBrand(forProperty("brand")) : null;
    }

}

