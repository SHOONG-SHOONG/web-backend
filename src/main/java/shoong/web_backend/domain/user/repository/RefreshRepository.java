package shoong.web_backend.domain.user.repository;

import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shoong.web_backend.domain.user.entity.Refresh;

@Repository
public interface RefreshRepository extends JpaRepository<Refresh, Long> {
    List<Refresh> findByUsername(String username);
    Boolean existsByRefresh(String refresh);
    @Transactional
    void deleteByRefresh(String refresh);
}
