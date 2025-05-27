package shoong.web_backend.domain.item.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QItem is a Querydsl query type for Item
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QItem extends EntityPathBase<Item> {

    private static final long serialVersionUID = 727908136L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QItem item = new QItem("item");

    public final shoong.web_backend.domain.brand.entity.QBrand brand;

    public final ListPath<shoong.web_backend.domain.cart.entity.Cart, shoong.web_backend.domain.cart.entity.QCart> carts = this.<shoong.web_backend.domain.cart.entity.Cart, shoong.web_backend.domain.cart.entity.QCart>createList("carts", shoong.web_backend.domain.cart.entity.Cart.class, shoong.web_backend.domain.cart.entity.QCart.class, PathInits.DIRECT2);

    public final StringPath category = createString("category");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final DateTimePath<java.time.LocalDateTime> discountExpiredAt = createDateTime("discountExpiredAt", java.time.LocalDateTime.class);

    public final NumberPath<Double> discountRate = createNumber("discountRate", Double.class);

    public final NumberPath<Long> itemId = createNumber("itemId", Long.class);

    public final ListPath<shoong.web_backend.domain.item_image.entity.ItemImage, shoong.web_backend.domain.item_image.entity.QItemImage> itemImages = this.<shoong.web_backend.domain.item_image.entity.ItemImage, shoong.web_backend.domain.item_image.entity.QItemImage>createList("itemImages", shoong.web_backend.domain.item_image.entity.ItemImage.class, shoong.web_backend.domain.item_image.entity.QItemImage.class, PathInits.DIRECT2);

    public final StringPath itemName = createString("itemName");

    public final NumberPath<Integer> itemQuantity = createNumber("itemQuantity", Integer.class);

    public final ListPath<shoong.web_backend.domain.live_item.entity.LiveItem, shoong.web_backend.domain.live_item.entity.QLiveItem> liveItems = this.<shoong.web_backend.domain.live_item.entity.LiveItem, shoong.web_backend.domain.live_item.entity.QLiveItem>createList("liveItems", shoong.web_backend.domain.live_item.entity.LiveItem.class, shoong.web_backend.domain.live_item.entity.QLiveItem.class, PathInits.DIRECT2);

    public final ListPath<shoong.web_backend.domain.order_item.entity.OrderItem, shoong.web_backend.domain.order_item.entity.QOrderItem> orderItems = this.<shoong.web_backend.domain.order_item.entity.OrderItem, shoong.web_backend.domain.order_item.entity.QOrderItem>createList("orderItems", shoong.web_backend.domain.order_item.entity.OrderItem.class, shoong.web_backend.domain.order_item.entity.QOrderItem.class, PathInits.DIRECT2);

    public final NumberPath<Long> price = createNumber("price", Long.class);

    public final EnumPath<shoong.web_backend.domain.item.enums.ItemStatus> status = createEnum("status", shoong.web_backend.domain.item.enums.ItemStatus.class);

    public final ListPath<shoong.web_backend.domain.wishlist.entity.Wishlist, shoong.web_backend.domain.wishlist.entity.QWishlist> wishlists = this.<shoong.web_backend.domain.wishlist.entity.Wishlist, shoong.web_backend.domain.wishlist.entity.QWishlist>createList("wishlists", shoong.web_backend.domain.wishlist.entity.Wishlist.class, shoong.web_backend.domain.wishlist.entity.QWishlist.class, PathInits.DIRECT2);

    public QItem(String variable) {
        this(Item.class, forVariable(variable), INITS);
    }

    public QItem(Path<? extends Item> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QItem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QItem(PathMetadata metadata, PathInits inits) {
        this(Item.class, metadata, inits);
    }

    public QItem(Class<? extends Item> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.brand = inits.isInitialized("brand") ? new shoong.web_backend.domain.brand.entity.QBrand(forProperty("brand")) : null;
    }

}

