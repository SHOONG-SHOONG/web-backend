package shoong.web_backend.domain.item.repository;// example

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import shoong.web_backend.domain.item.entity.Item;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemRepositoryCustom {
    Optional<Item> findByItemId(Long itemId);
    List<Item> findByCategory(String category);
}