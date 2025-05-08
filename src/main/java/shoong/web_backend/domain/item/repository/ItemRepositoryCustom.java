package shoong.web_backend.domain.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import shoong.web_backend.domain.item.condition.ItemSearchCondition;
import shoong.web_backend.domain.item.dto.ItemResponseDto;

public interface ItemRepositoryCustom {
    Page<ItemResponseDto> searchItems(ItemSearchCondition condition, Pageable pageable);
}
