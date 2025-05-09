package shoong.web_backend.domain.wishlist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shoong.web_backend.domain.item.entity.Item;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.wishlist.entity.Wishlist;

import java.util.List;
import java.util.Optional;


public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findAllByUserId(Long userId);
    boolean existsByUserAndItem(User user, Item item);
    Optional<Wishlist> findByUserIdAndItemItemId(Long userId, Long itemId);

}
