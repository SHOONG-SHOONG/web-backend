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
public class LiveDetailDto {
    private long id;
    private String title;
    private String description;
    private String imageUrl;
    private String streamKey;
    private LocalDateTime liveStartTime;
    private LocalDateTime liveEndTime;
    private LocalDate liveDate;
    private LiveStatus liveStatus;
    private String replayURL;
    private List<LiveItemResponseDto> liveItems;
}
