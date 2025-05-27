package shoong.web_backend.domain.live_item.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLiveItem is a Querydsl query type for LiveItem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLiveItem extends EntityPathBase<LiveItem> {

    private static final long serialVersionUID = -1999677699L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLiveItem liveItem = new QLiveItem("liveItem");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final shoong.web_backend.domain.item.entity.QItem item;

    public final shoong.web_backend.domain.live.entity.QLive live;

    public QLiveItem(String variable) {
        this(LiveItem.class, forVariable(variable), INITS);
    }

    public QLiveItem(Path<? extends LiveItem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLiveItem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLiveItem(PathMetadata metadata, PathInits inits) {
        this(LiveItem.class, metadata, inits);
    }

    public QLiveItem(Class<? extends LiveItem> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.item = inits.isInitialized("item") ? new shoong.web_backend.domain.item.entity.QItem(forProperty("item"), inits.get("item")) : null;
        this.live = inits.isInitialized("live") ? new shoong.web_backend.domain.live.entity.QLive(forProperty("live"), inits.get("live")) : null;
    }

}

