package shoong.web_backend.domain.item.dto;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import shoong.web_backend.domain.item_image.dto.ItemImageDto;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.constraints.*;


@Getter
@Setter
public class ItemRequestDto {

    @NotBlank(message = "상품명은 필수 항목입니다.")
    private String itemName;

    @NotNull(message = "가격은 필수 항목입니다.")
    @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
    private Long price;

    @NotNull(message = "할인율은 필수 항목입니다.")
    @DecimalMin(value = "0.0", inclusive = true, message = "할인율은 0 이상이어야 합니다.")
    @DecimalMax(value = "1.0", inclusive = true, message = "할인율은 1 이하여야 합니다.")
    private Double discountRate;

    @NotBlank(message = "설명은 필수 항목입니다.")
    private String description;

    @NotNull(message = "수량은 필수 항목입니다.")
    @Min(value = 0, message = "수량은 0개 이상이어야 합니다.")
    private Integer itemQuantity;

    @NotBlank(message = "카테고리는 필수 항목입니다.")
    private String category;

    @NotNull(message = "등록일시는 필수 항목입니다.")
    private LocalDateTime createdAt;

    @Future(message = "할인 종료일은 미래의 시간이어야 합니다.")
    private LocalDateTime discountExpiredAt;

//    @NotNull(message = "이미지 리스트는 null일 수 없습니다.")
//    @Size(min = 1, message = "최소 1개 이상의 이미지가 필요합니다.")
//    private List<@Valid ItemImageDto> itemImages;
}

