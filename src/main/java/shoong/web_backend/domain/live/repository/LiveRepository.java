package shoong.web_backend.domain.live.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import shoong.web_backend.domain.live.entity.Live;
import shoong.web_backend.domain.live.enums.LiveStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LiveRepository extends JpaRepository<Live, Long> {

    Optional<Live> findFirstByLiveStatusOrderByLiveStartTimeAsc(LiveStatus status);

    Optional<Live> findFirstByLiveStatusAndLiveStartTimeAfterOrderByLiveStartTimeAsc(
            LiveStatus status, LocalDateTime now);

    // 종료된 라이브 N개 조회(최근 종료된 순서대로)
    @Query("SELECT l FROM Live l WHERE l.liveStatus = :status ORDER BY l.liveEndTime DESC")
    List<Live> findTopNByLiveStatusOrderByLiveEndTimeDesc(LiveStatus status, Pageable pageable);

    default List<Live> findTopNByLiveStatusOrderByLiveEndTimeDesc(LiveStatus status, int n) {
        return findTopNByLiveStatusOrderByLiveEndTimeDesc(status, PageRequest.of(0, n));
    }

    List<Live> findAllByLiveDateOrderByLiveStartTimeAsc(LocalDate liveDate);

    Optional<Live> findFirstByUserIdAndLiveStatus(Long userId, LiveStatus status);
    // 제목 + 최신 목록
    Optional<Live> findFirstByTitleContainingIgnoreCaseOrderByLiveDateDesc(String title);

    // 제목없이 최신 목록
    Optional<Live> findFirstByOrderByLiveStartTimeDesc();

    List<Live> findAllByUserIdOrderByLiveStartTimeDesc(Long userId);
    Optional<Live> findTopByStreamKeyOrderByLiveStartTimeDesc(String streamKey);

}
