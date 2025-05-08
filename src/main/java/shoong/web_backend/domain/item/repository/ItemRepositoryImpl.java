package shoong.web_backend.domain.item.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import shoong.web_backend.domain.item.condition.ItemSearchCondition;
import shoong.web_backend.domain.item.dto.ItemResponseDto;
import shoong.web_backend.domain.item.entity.QItem;
import shoong.web_backend.domain.item_image.entity.ItemImage;
import shoong.web_backend.domain.item_image.entity.QItemImage;
import shoong.web_backend.domain.wishlist.entity.QWishlist;

@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ItemResponseDto> searchItems(ItemSearchCondition condition, Pageable pageable) {
        QItem item = QItem.item;
        QItemImage itemImage = QItemImage.itemImage;
        QWishlist wishlist = QWishlist.wishlist;

        // 계산된 최종 가격 표현식 (할인율 적용)
        NumberExpression<Integer> finalPrice = Expressions.numberTemplate(Integer.class,
                "CAST({0} * (1 - COALESCE({1}, 0)) AS INTEGER)",
                item.price, item.discountRate);

        // 동적 쿼리 생성을 위한 BooleanBuilder
        BooleanBuilder whereCondition = new BooleanBuilder();

        // 카테고리 필터링
        if (StringUtils.hasText(condition.getCategory())) {
            whereCondition.and(item.category.eq(condition.getCategory()));
        }

        // 키워드 검색 (상품명, 설명에서 검색)
        if (StringUtils.hasText(condition.getKeyword())) {
            whereCondition.and(
                    item.itemName.containsIgnoreCase(condition.getKeyword())
                            .or(item.description.containsIgnoreCase(condition.getKeyword()))
            );
        }

        // 가격 범위 검색
        if (condition.getMinPrice() != null) {
            whereCondition.and(finalPrice.goe(condition.getMinPrice().intValue()));
        }
        if (condition.getMaxPrice() != null) {
            whereCondition.and(finalPrice.loe(condition.getMaxPrice().intValue()));
        }

        NumberExpression<Long> wishlistCountExpr = Expressions.numberTemplate(Long.class,
                "CAST({0} AS LONG)",
                queryFactory
                        .select(wishlist.count())
                        .from(wishlist)
                        .where(wishlist.item.eq(item)));

        // 기본 쿼리 작성
        JPAQuery<ItemResponseDto> query = queryFactory
                .select(Projections.constructor(ItemResponseDto.class,
                        item.itemId,
                        item.brand.brandId.as("brandId"),
                        item.itemName,
                        item.price,
                        item.discountRate,
                        finalPrice.as("finalPrice"),
                        queryFactory.select(wishlist.count().as("wishlistCount"))
                                .from(wishlist)
                                .where(wishlist.item.eq(item)),
                        item.description,
                        item.itemQuantity,
                        item.category,
                        item.discountExpiredAt,
                        item.status
                ))
                .from(item)
                .where(whereCondition);

        // 정렬 조건 적용
        if (condition.getSortBy() != null) {
            switch (condition.getSortBy()) {
                case "createdAt":
                    query.orderBy(item.createdAt.desc());
                    break;
                case "priceAsc":
                    query.orderBy(finalPrice.asc());
                    break;
                case "priceDesc":
                    query.orderBy(finalPrice.desc());
                    break;
                case "wishlist":
                    query.orderBy(wishlistCountExpr.desc());  // 찜 수로 내림차순 정렬
                    break;
                default:
                    query.orderBy(item.createdAt.desc()); // 기본 정렬: 최신순
            }
        } else {
            query.orderBy(item.createdAt.desc()); // 기본 정렬: 최신순
        }

        // 페이징 적용
        long total = query.fetchCount();
        List<ItemResponseDto> results = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 각 상품에 대한 이미지 정보 첨부
        for (ItemResponseDto dto : results) {
            List<ItemImage> images = queryFactory
                    .selectFrom(itemImage)
                    .where(itemImage.item.itemId.eq(dto.getItemId()))
                    .fetch();
            dto.setItemImages(images);
        }

        return new PageImpl<>(results, pageable, total);
    }
}
