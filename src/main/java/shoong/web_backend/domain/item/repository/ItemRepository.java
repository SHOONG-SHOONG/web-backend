package shoong.web_backend.domain.item.repository;// example

import org.springframework.data.jpa.repository.JpaRepository;
import shoong.web_backend.domain.item.entity.Item;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findByItemId(Long itemId);
}
