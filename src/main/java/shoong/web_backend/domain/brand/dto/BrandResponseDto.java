package shoong.web_backend.domain.brand.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import shoong.web_backend.domain.item.entity.Item;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class BrandResponseDto {
    private String brandName;
    private String brandDescription;
    private String logoUrl;
    private List<BrandItemDto> items;
}
