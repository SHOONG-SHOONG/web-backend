package shoong.web_backend.domain.brand.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BrandItemDto {
    private Long itemId;
    private String name;
    private Long price;
    private String imageUrl;
}
