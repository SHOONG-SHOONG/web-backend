package shoong.web_backend.domain.orders.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shoong.web_backend.domain.orders.entity.Orders;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
}
