package shoong.web_backend.domain.live_item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shoong.web_backend.domain.live_item.entity.LiveItem;

public interface LiveItemRepository extends JpaRepository<LiveItem, Long> {}