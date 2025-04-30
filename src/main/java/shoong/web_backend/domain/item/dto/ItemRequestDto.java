package shoong.web_backend.domain.item.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import shoong.web_backend.domain.item.enums.ItemStatus;

import java.time.LocalDateTime;

public class ItemRequestDto {
    private String itemName;

    private Long price;

    private Double discountRate;

    private String description;

    private Integer itemQuantity;

    private String category;

    private LocalDateTime createdAt;

    private LocalDateTime discountExpiredAt;

    @Enumerated(EnumType.STRING)
    private ItemStatus status;
}
