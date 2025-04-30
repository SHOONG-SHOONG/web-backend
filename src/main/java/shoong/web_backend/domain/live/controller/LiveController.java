package shoong.web_backend.domain.live.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shoong.web_backend.domain.live.dto.LiveCreateRequestDto;
import shoong.web_backend.domain.live.dto.LiveCreateResponseDto;
import shoong.web_backend.domain.live.service.LiveService;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.enums.UserRole;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lives")
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

}