package shoong.web_backend.domain.live.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import shoong.web_backend.domain.live.enums.LiveStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class LiveCreateResponseDto {
    private long id;
    private String title;
    private String description;
    private String imageUrl;
    private LocalDate liveDate;
    private LocalDateTime liveStartTime;
    private LiveStatus liveStatus;
}
