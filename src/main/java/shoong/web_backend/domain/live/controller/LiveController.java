package shoong.web_backend.domain.live.controller;

import io.swagger.v3.oas.annotations.Operation;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import shoong.web_backend.domain.live.dto.*;
import shoong.web_backend.domain.live.service.LiveService;
import shoong.web_backend.domain.user.dto.form.CustomUserDetails;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/live")
public class LiveController {

    private final LiveService liveService;
    private final UserRepository userRepository;

    @Operation(summary = "라이브 생성", description = "라이브 방송 생성 API")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LiveCreateResponseDto> createLive(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "itemIds", required = false) List<Long> itemIds,
            @RequestParam(value = "streamKey", required = true) String streamKey,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
            ) {
        // DTO로 변환

        LiveCreateRequestDto requestDto = new LiveCreateRequestDto(title, description,
                imageFile, LocalDate.now(), LocalDateTime.now(), itemIds, streamKey);

        User user = userRepository.findById(customUserDetails.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 유저가 존재하지 않습니다."));

        LiveCreateResponseDto responseDto = liveService.createLive(requestDto, user);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/main")
    public ResponseEntity<LiveMainDto> getMainLiveList() {
        LiveMainDto mainLiveList = liveService.getOngoingLiveMainDto();
        return ResponseEntity.ok(mainLiveList);
    }

    @GetMapping("/list")
    public ResponseEntity<List<LiveMainDto>> getCompletedLiveList() {
        List<LiveMainDto> completedLiveList = liveService.getCompLiveList();
        return ResponseEntity.ok(completedLiveList);
    }

    @Description("date 형식 예시 : 2025-05-01")
    @GetMapping("/scheduled")
    public List<LiveScheduledDto> getLiveScheduledByDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                         LocalDate date) {
        return liveService.getLiveScheduledByDate(date);
    }

    @GetMapping("/brand/live-onGoing/{brandId}")
    public ResponseEntity<Object> checkLiveOngoingByBrandId(@PathVariable Long brandId) {
        Optional<LiveMainDto> ongoingLiveDto = liveService.getLiveOngoingByBrandId(brandId);

        if (ongoingLiveDto.isPresent()) {
            return ResponseEntity.ok(ongoingLiveDto.get());  // 라이브 정보 반환
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No live ongoing for this brand");  // 진행 중이지 않음
        }
    }
    // ✅ 방송 제목으로 스트림 키 조회
    @GetMapping("/stream-key/search")
    public ResponseEntity<String> getStreamKeyByTitle(@RequestParam("title") String title) {
        String streamKey = liveService.searchStreamKeyByTitle(title);
        return ResponseEntity.ok(streamKey);
    }
    // ✅ 최신 방송 스트림 키 조회
    @GetMapping("/stream-key/latest")
    public ResponseEntity<String> getLatestStreamKey() {
        String streamKey = liveService.getLatestStreamKey();
        return ResponseEntity.ok(streamKey);
    }

    @Operation(summary = "사용자의 모든 라이브 목록 조회", description = "현재 인증된 사용자의 모든 라이브 방송 목록을 상태 구분 없이 조회합니다.")
    @GetMapping("/my-lives")
    public ResponseEntity<List<LiveMainDto>> getAllLivesByUser(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        System.out.println(customUserDetails.getUserId());
        User user = userRepository.findById(customUserDetails.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 유저가 존재하지 않습니다."));

        List<LiveMainDto> lives = liveService.getAllLivesByUser(user);
        return ResponseEntity.ok(lives);
    }

    @PostMapping("/vods")
    public ResponseEntity<Void> saveVOD(@RequestBody VodRequestDto request) {
        liveService.updateReplayUrlByStreamKey(request.getStreamKey(), request.getVodUrl());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{liveid}")
    public ResponseEntity<LiveDetailDto> getLiveById(@PathVariable Long liveid) {
        LiveDetailDto live = liveService.getLiveById(liveid);
        return ResponseEntity.ok(live);
    }

    @Operation(summary = "라이브 종료 처리", description = "라이브 방송을 완료 상태로 변경합니다.")
    @PostMapping("/complete/{liveId}")
    public ResponseEntity<Void> completeLive(@PathVariable Long liveId) {
        liveService.completeLive(liveId);
        return ResponseEntity.ok().build();
    }
}
