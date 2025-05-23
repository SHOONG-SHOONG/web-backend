package shoong.web_backend.domain.cart.repository;// example

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shoong.web_backend.domain.cart.entity.Cart;

import java.util.List;
import java.util.Optional;
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findAllByUserId(Long userId);

    void deleteAllByUserId(Long userId);

    Optional<Cart> findByUserIdAndItem_ItemId(Long userId, Long itemId);
    // 가장 최근에 생성한 카트만 가져오기
    Cart findTopByUserIdOrderByCartIdDesc(Long userId);

    List<Cart> findByUserIdAndCartIdIn(Long userId, List<Long> cartIds);

    List<Cart> findByUserId(Long userId);

    void deleteByCartIdIn(List<Long> cartIds);


}
