package shoong.web_backend.domain.item_image.repository;// example

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import shoong.web_backend.domain.item_image.entity.ItemImage;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {
    // ✅ 정확한 필드명을 반영한 쿼리 메서드
    List<ItemImage> findByItem_ItemId(Long itemId);
}