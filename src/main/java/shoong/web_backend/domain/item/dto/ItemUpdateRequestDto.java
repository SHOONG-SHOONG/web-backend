package shoong.web_backend.domain.item.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
public class ItemUpdateRequestDto {
    @NotBlank(message = "상품명은 필수 항목입니다.")
    private String itemName;

    @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
    private Long price;

    @DecimalMin(value = "0.0", inclusive = true, message = "할인율은 0 이상이어야 합니다.")
    @DecimalMax(value = "1.0", inclusive = true, message = "할인율은 1 이하여야 합니다.")
    private Double discountRate;

    private String description;

    @Min(value = 0, message = "수량은 0개 이상이어야 합니다.")
    private Integer itemQuantity;

    private String category;

}
