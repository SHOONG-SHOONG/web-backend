package shoong.web_backend.domain.admin.dto;

import lombok.Builder;
import lombok.Data;
import shoong.web_backend.domain.item.enums.ItemStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminItemDto {

    private Long itemId;
    private String itemName;
    private Long price;
    private Double discountRate;
    private String description;
    private Integer itemQuantity;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime discountExpiredAt;
    private ItemStatus status;
    private Long brandId;
    private String brandName;
}