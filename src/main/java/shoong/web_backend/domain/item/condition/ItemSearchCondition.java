package shoong.web_backend.domain.item.condition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
/*
 * 동적 쿼리를 위한 조건 클래스
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemSearchCondition {
    private String category;        // 카테고리
    private String keyword;         // 검색 키워드
    private Long minPrice;          // 최소 가격
    private Long maxPrice;          // 최대 가격
    private String sortBy;
    private Long brandId;
    // 정렬 기준 (createdAt, priceAsc, priceDesc)
}
