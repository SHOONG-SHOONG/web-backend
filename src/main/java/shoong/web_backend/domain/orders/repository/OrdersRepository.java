package shoong.web_backend.domain.orders.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shoong.web_backend.domain.orders.entity.Orders;
import shoong.web_backend.domain.orders.enums.OrderStatus;
import shoong.web_backend.domain.wishlist.entity.Wishlist;

import java.util.List;
import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
    Optional<Orders> findByUserIdAndOrderStatus(Long userId, OrderStatus orderStatus);
}