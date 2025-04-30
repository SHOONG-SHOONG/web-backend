package shoong.web_backend.domain.live.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shoong.web_backend.domain.live.entity.Live;

public interface LiveRepository extends JpaRepository<Live, Long> {

}