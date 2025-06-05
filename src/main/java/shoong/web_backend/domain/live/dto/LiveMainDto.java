package shoong.web_backend.domain.live.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import shoong.web_backend.domain.live.enums.LiveStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@SuperBuilder
public class LiveMainDto {
    private long liveId;
    private long itemId;
    private String title;
    private String imageUrl;
    private String itemName;
    private Long price;
    private Double discountRate;
    private String itemImageUrl;
    private LiveStatus status;
    private LocalDate liveDate;
    private LocalDateTime liveStartTime;
    private LocalDateTime liveEndTime;
}
