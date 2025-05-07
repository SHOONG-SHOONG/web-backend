package shoong.web_backend.domain.live.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LiveMainDto {
    private long id;
    private String title;
    private String imageUrl;
}
