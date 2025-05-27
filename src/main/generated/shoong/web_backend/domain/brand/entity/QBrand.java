package shoong.web_backend.domain.brand.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBrand is a Querydsl query type for Brand
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBrand extends EntityPathBase<Brand> {

    private static final long serialVersionUID = -1134499192L;

    public static final QBrand brand = new QBrand("brand");

    public final StringPath brandDescription = createString("brandDescription");

    public final NumberPath<Long> brandId = createNumber("brandId", Long.class);

    public final StringPath brandName = createString("brandName");

    public final ListPath<shoong.web_backend.domain.item.entity.Item, shoong.web_backend.domain.item.entity.QItem> items = this.<shoong.web_backend.domain.item.entity.Item, shoong.web_backend.domain.item.entity.QItem>createList("items", shoong.web_backend.domain.item.entity.Item.class, shoong.web_backend.domain.item.entity.QItem.class, PathInits.DIRECT2);

    public final StringPath logoUrl = createString("logoUrl");

    public QBrand(String variable) {
        super(Brand.class, forVariable(variable));
    }

    public QBrand(Path<? extends Brand> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBrand(PathMetadata metadata) {
        super(Brand.class, metadata);
    }

}

