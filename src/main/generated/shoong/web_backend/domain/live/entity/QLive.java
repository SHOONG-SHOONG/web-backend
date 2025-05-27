package shoong.web_backend.domain.live.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLive is a Querydsl query type for Live
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLive extends EntityPathBase<Live> {

    private static final long serialVersionUID = 2135846234L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLive live = new QLive("live");

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final DatePath<java.time.LocalDate> liveDate = createDate("liveDate", java.time.LocalDate.class);

    public final DateTimePath<java.time.LocalDateTime> liveEndTime = createDateTime("liveEndTime", java.time.LocalDateTime.class);

    public final ListPath<shoong.web_backend.domain.live_item.entity.LiveItem, shoong.web_backend.domain.live_item.entity.QLiveItem> liveItems = this.<shoong.web_backend.domain.live_item.entity.LiveItem, shoong.web_backend.domain.live_item.entity.QLiveItem>createList("liveItems", shoong.web_backend.domain.live_item.entity.LiveItem.class, shoong.web_backend.domain.live_item.entity.QLiveItem.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> liveStartTime = createDateTime("liveStartTime", java.time.LocalDateTime.class);

    public final EnumPath<shoong.web_backend.domain.live.enums.LiveStatus> liveStatus = createEnum("liveStatus", shoong.web_backend.domain.live.enums.LiveStatus.class);

    public final StringPath replayURL = createString("replayURL");

    public final StringPath streamKey = createString("streamKey");

    public final StringPath title = createString("title");

    public final shoong.web_backend.domain.user.entity.QUser user;

    public QLive(String variable) {
        this(Live.class, forVariable(variable), INITS);
    }

    public QLive(Path<? extends Live> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLive(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLive(PathMetadata metadata, PathInits inits) {
        this(Live.class, metadata, inits);
    }

    public QLive(Class<? extends Live> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new shoong.web_backend.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

