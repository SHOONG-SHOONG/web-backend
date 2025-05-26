package shoong.web_backend.domain.item.repository;// example

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import shoong.web_backend.domain.item.entity.Item;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemRepositoryCustom {
    Optional<Item> findByItemId(Long itemId);
    List<Item> findByCategory(String category);

    // 브랜드 아이디로 아이템 리스트 조회
    List<Item> findByBrand_BrandId(Long brandId);
}