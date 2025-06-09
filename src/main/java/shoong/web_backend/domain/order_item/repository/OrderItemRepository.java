package shoong.web_backend.domain.order_item.repository;// example

import org.springframework.data.jpa.repository.JpaRepository;
import shoong.web_backend.domain.order_item.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}