package shoong.web_backend.domain.live.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class VodRequestDto {
    private String streamKey;
    private String vodUrl;
}
