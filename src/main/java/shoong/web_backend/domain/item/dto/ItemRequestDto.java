package shoong.web_backend.domain.item.dto;

import lombok.Getter;
import lombok.Setter;
import shoong.web_backend.domain.item_image.dto.ItemImageDto;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ItemRequestDto {
    private Long brandId;

    private String itemName;

    private Long price;

    private Double discountRate;

    private String description;

    private Integer itemQuantity;

    private String category;

    private LocalDateTime createdAt;

    private LocalDateTime discountExpiredAt;

    private List<ItemImageDto> itemImages;
}
