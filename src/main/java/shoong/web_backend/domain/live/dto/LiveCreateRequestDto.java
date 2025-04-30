package shoong.web_backend.domain.live.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class LiveCreateRequestDto {
    private String title;
    private String description;
    private String imageUrl;
    private LocalDate liveDate;
    private LocalDateTime liveStartTime;
    private LocalDateTime liveEndTime;
}