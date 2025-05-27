package shoong.web_backend.domain.orders.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrders is a Querydsl query type for Orders
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrders extends EntityPathBase<Orders> {

    private static final long serialVersionUID = -319151924L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrders orders = new QOrders("orders");

    public final StringPath orderAddress = createString("orderAddress");

    public final DateTimePath<java.time.LocalDateTime> orderDate = createDateTime("orderDate", java.time.LocalDateTime.class);

    public final NumberPath<Long> orderId = createNumber("orderId", Long.class);

    public final ListPath<shoong.web_backend.domain.order_item.entity.OrderItem, shoong.web_backend.domain.order_item.entity.QOrderItem> orderItems = this.<shoong.web_backend.domain.order_item.entity.OrderItem, shoong.web_backend.domain.order_item.entity.QOrderItem>createList("orderItems", shoong.web_backend.domain.order_item.entity.OrderItem.class, shoong.web_backend.domain.order_item.entity.QOrderItem.class, PathInits.DIRECT2);

    public final NumberPath<Long> totalPrice = createNumber("totalPrice", Long.class);

    public final shoong.web_backend.domain.user.entity.QUser user;

    public QOrders(String variable) {
        this(Orders.class, forVariable(variable), INITS);
    }

    public QOrders(Path<? extends Orders> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrders(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrders(PathMetadata metadata, PathInits inits) {
        this(Orders.class, metadata, inits);
    }

    public QOrders(Class<? extends Orders> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new shoong.web_backend.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

