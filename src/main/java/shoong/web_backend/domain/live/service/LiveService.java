package shoong.web_backend.domain.live.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shoong.web_backend.domain.live.dto.LiveCreateRequestDto;
import shoong.web_backend.domain.live.dto.LiveCreateResponseDto;
import shoong.web_backend.domain.live.entity.Live;
import shoong.web_backend.domain.live.enums.LiveStatus;
import shoong.web_backend.domain.live.repository.LiveRepository;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.enums.UserRole;

@Service
@RequiredArgsConstructor
public class LiveService {

    private final LiveRepository liveRepository;

    @Transactional
    public LiveCreateResponseDto createLive(LiveCreateRequestDto liveCreateRequestDto, User user) {
        if(user.getRole() == null || !user.getRole().equals(UserRole.STREAMER)) {
            throw new IllegalArgumentException("스트리머 권한이 있는 사용자만 라이브를 등록할 수 있습니다.");
        }

        Live live = Live.builder()
                .title(liveCreateRequestDto.getTitle())
                .description(liveCreateRequestDto.getDescription())
                .imageUrl(liveCreateRequestDto.getImageUrl())
                .liveDate(liveCreateRequestDto.getLiveDate())
                .liveStartTime(liveCreateRequestDto.getLiveStartTime())
                .liveEndTime(liveCreateRequestDto.getLiveEndTime())
                .liveStatus(LiveStatus.SCHEDULED)
                .user(user)
                .build();

        Live savedLive = liveRepository.save(live);

        return new LiveCreateResponseDto(
                savedLive.getId(),
                savedLive.getTitle(),
                savedLive.getImageUrl(),
                savedLive.getDescription(),
                savedLive.getLiveDate(),
                savedLive.getLiveStartTime(),
                savedLive.getLiveStatus()
        );
    }
}