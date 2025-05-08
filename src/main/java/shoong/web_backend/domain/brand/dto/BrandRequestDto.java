package shoong.web_backend.domain.brand.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandRequestDto {

    private String brandName;
    private String brandDescription;
    private MultipartFile imageFile;
}