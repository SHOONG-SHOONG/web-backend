package shoong.web_backend.domain.live.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LiveCreateRequestDto {
    private String title;
    private String description;
    private MultipartFile imageFile; // 실제 이미지 파일 업로드용 필드 추가
    private LocalDate liveDate;
    private LocalDateTime liveStartTime;
    private List<Long> itemIds;
}