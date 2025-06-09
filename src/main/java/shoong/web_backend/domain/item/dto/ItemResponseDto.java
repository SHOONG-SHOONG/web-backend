package shoong.web_backend.domain.item.dto;

import java.util.ArrayList;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Setter;
import shoong.web_backend.domain.item.enums.ItemStatus;
import shoong.web_backend.domain.item_image.entity.ItemImage;

@Getter
@Setter
@NoArgsConstructor
public class ItemResponseDto {

    private Long itemId;
    private Long brandId;
    private String itemName;
    private Long price;
    private Double discountRate;
    private Integer finalPrice;
    private Integer wishlistCount;
    private String description;
    private Integer itemQuantity;
    private String category;
    private LocalDateTime discountExpiredAt;
    private ItemStatus status; // 혹은 enum ItemStatus로 변경 가능
    private List<ItemImage> itemImages = new ArrayList<>();

    public ItemResponseDto(Long itemId, Long brandId, String itemName, Long price, Double discountRate,
                           Integer finalPrice, Long wishlistCount, String description, Integer itemQuantity,
                           String category, LocalDateTime discountExpiredAt, ItemStatus status) {
        this.itemId = itemId;
        this.brandId = brandId;
        this.itemName = itemName;
        this.price = price;
        this.discountRate = discountRate;
        this.finalPrice = finalPrice;
        this.wishlistCount = wishlistCount != null ? wishlistCount.intValue() : 0;
        this.description = description;
        this.itemQuantity = itemQuantity;
        this.category = category;
        this.discountExpiredAt = discountExpiredAt;
        this.status = status;
    }
    @Builder
    public ItemResponseDto(Long itemId, Long brandId, String itemName, Long price, Double discountRate,
                           Integer finalPrice, Integer wishlistCount, String description, Integer itemQuantity,
                           String category, LocalDateTime discountExpiredAt, ItemStatus status, List<ItemImage> itemImages) {
        this.itemId = itemId;
        this.brandId = brandId;
        this.itemName = itemName;
        this.price = price;
        this.discountRate = discountRate;
        this.finalPrice = finalPrice;
        this.wishlistCount = wishlistCount;
        this.description = description;
        this.itemQuantity = itemQuantity;
        this.category = category;
        this.discountExpiredAt = discountExpiredAt;
        this.status = status;
        this.itemImages = itemImages != null ? itemImages : new ArrayList<>();
    }
}
