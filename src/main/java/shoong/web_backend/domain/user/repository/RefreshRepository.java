package shoong.web_backend.domain.user.repository;

import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shoong.web_backend.domain.user.entity.Refresh;

@Repository
public interface RefreshRepository extends JpaRepository<Refresh, Long> {
    Boolean existsByRefresh(String refresh);
    @Transactional
    void deleteByRefresh(String refresh);
    Optional<Refresh> findByUsername(String username); // 이 메서드 추가
}
