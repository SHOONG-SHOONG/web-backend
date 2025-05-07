package shoong.web_backend.domain.live.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import shoong.web_backend.domain.live.enums.LiveStatus;
import shoong.web_backend.domain.live_item.dto.LiveItemResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class LiveScheduledDto {
    private long id;
    private String title;
    private String brandName; // user에서 브랜드 이름
    private String imageUrl;
    private LocalDateTime liveStartTime;
    private LocalDate liveDate;
    private LiveStatus liveStatus;
    private List<LiveItemResponseDto> liveItems;

}
