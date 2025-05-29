package shoong.web_backend.domain.live_item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LiveItemResponseDto {
    private long itemId;
    private String itemName;
    private String imageUrl;
    private Long price;
    private Double discountRate;
}