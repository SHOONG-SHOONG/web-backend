package shoong.web_backend.domain.cart.repository;// example

import org.springframework.data.jpa.repository.JpaRepository;
import shoong.web_backend.domain.cart.entity.Cart;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findAllByUserId(Long userId);

    void deleteAllByUserId(Long userId);
}
