package shoong.web_backend.domain.live.controller;

import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shoong.web_backend.domain.live.dto.LiveCreateRequestDto;
import shoong.web_backend.domain.live.dto.LiveCreateResponseDto;
import shoong.web_backend.domain.live.dto.LiveMainDto;
import shoong.web_backend.domain.live.dto.LiveScheduledDto;
import shoong.web_backend.domain.live.service.LiveService;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.enums.UserRole;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/live")
public class LiveController {

    private final LiveService liveService;

    // 테스트용 유저 생성 (스트리머 권한)
    User mockUser = User.builder()
            .id(1L)
            .userEmail("test@streamer.com")
            .userName("테스트스트리머")
            .role(UserRole.STREAMER)
            .build();

    @PostMapping("/create")
    public ResponseEntity<LiveCreateResponseDto> createLive(
            @RequestBody LiveCreateRequestDto liveCreateRequestDto
//            @RequestAttribute("user") User user // 테스트 용으로 주석 해둠 나중에 유저 처리
    ) {
        LiveCreateResponseDto responseDto = liveService.createLive(liveCreateRequestDto, mockUser);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/main")
    public ResponseEntity<List<LiveMainDto>> getMainLiveList() {
        List<LiveMainDto> mainLiveList = liveService.getMainLiveList();
        return ResponseEntity.ok(mainLiveList);
    }

    @Description("date 형식 예시 : 2025-05-01")
    @GetMapping("/scheduled")
    public List<LiveScheduledDto> getLiveScheduledByDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                         LocalDate date) {
        return liveService.getLiveScheduledByDate(date);
    }

    @GetMapping("/brand/{brandId}/live-onGoing")
    public ResponseEntity<Object> checkLiveOngoingByBrandId(@PathVariable Long brandId) {
        Optional<LiveMainDto> ongoingLiveDto = liveService.getLiveOngoingByBrandId(brandId);

        if (ongoingLiveDto.isPresent()) {
            return ResponseEntity.ok(ongoingLiveDto.get());  // 라이브 정보 반환
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No live ongoing for this brand");  // 진행 중이지 않음
        }
    }
}