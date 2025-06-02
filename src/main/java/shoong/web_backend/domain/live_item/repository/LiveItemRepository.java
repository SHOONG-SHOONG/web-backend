package shoong.web_backend.domain.live_item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shoong.web_backend.domain.live.enums.LiveStatus;
import shoong.web_backend.domain.live_item.entity.LiveItem;

import java.util.List;
import java.util.Optional;

public interface LiveItemRepository extends JpaRepository<LiveItem, Long> {
    Optional<LiveItem> findFirstByLiveIdOrderByIdAsc(Long liveId);

    @Query("SELECT li FROM LiveItem li " +
            "WHERE li.item.id IN :itemIds " +
            "AND li.live.liveStatus = :liveStatus")
    List<LiveItem> findOngoingLiveItems(@Param("itemIds") List<Long> itemIds,
                                        @Param("liveStatus") LiveStatus liveStatus);
}