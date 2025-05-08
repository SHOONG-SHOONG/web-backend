package shoong.web_backend.domain.live.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import shoong.web_backend.aws.AmazonS3Manager;
import shoong.web_backend.domain.item.entity.Item;
import shoong.web_backend.domain.item.repository.ItemRepository;
import shoong.web_backend.domain.live.dto.LiveCreateRequestDto;
import shoong.web_backend.domain.live.dto.LiveCreateResponseDto;
import shoong.web_backend.domain.live.dto.LiveMainDto;
import shoong.web_backend.domain.live.dto.LiveScheduledDto;
import shoong.web_backend.domain.live.entity.Live;
import shoong.web_backend.domain.live.enums.LiveStatus;
import shoong.web_backend.domain.live.repository.LiveRepository;
import shoong.web_backend.domain.live_item.dto.LiveItemResponseDto;
import shoong.web_backend.domain.live_item.entity.LiveItem;
import shoong.web_backend.domain.live_item.repository.LiveItemRepository;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.enums.UserRole;
import shoong.web_backend.domain.user.repository.UserRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LiveService {

    private final LiveRepository liveRepository;
    private final UserRepository userRepository;
    private final AmazonS3Manager amazonS3Manager;
    private final ItemRepository itemRepository;
    private final LiveItemRepository liveItemRepository;

    @Transactional
    public LiveCreateResponseDto createLive(LiveCreateRequestDto liveCreateRequestDto, User user) {
        if(user.getRole() == null || !user.getRole().equals(UserRole.STREAMER)) {
            throw new IllegalArgumentException("스트리머 권한이 있는 사용자만 라이브를 등록할 수 있습니다.");
        }

        // 기본 이미지 URL (이미지가 없을 경우를 대비)
        String imageUrl = "https://기본이미지URL.jpg"; // 필요시 기본 이미지 URL 설정

        MultipartFile imageFile = liveCreateRequestDto.getImageFile();

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // S3에 라이브 이미지 업로드
                String keyName = amazonS3Manager.generateLiveKeyName();
                imageUrl = amazonS3Manager.upLoadFile(keyName, imageFile);
            } catch (IOException e) {
                throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다.", e);
            }
        }

        Live live = Live.builder()
                .title(liveCreateRequestDto.getTitle())
                .description(liveCreateRequestDto.getDescription())
                .imageUrl(imageUrl)
                .liveDate(liveCreateRequestDto.getLiveDate())
                .liveStartTime(liveCreateRequestDto.getLiveStartTime())
                .liveEndTime(null)
                .liveStatus(LiveStatus.SCHEDULED)
                .user(user)
                .build();

        Live savedLive = liveRepository.save(live);

        List<Long> itemIds = liveCreateRequestDto.getItemIds();
        List<Item> items = itemRepository.findAllById(itemIds);

        if (items.size() != itemIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 아이템 ID가 포함되어 있습니다.");
        }

        List<LiveItem> liveItems = items.stream()
                .map(item -> {
                    LiveItem liveItem = new LiveItem();
                    liveItem.setLive(savedLive);
                    liveItem.setItem(item);
                    return liveItem;
                })
                .collect(Collectors.toList());

        liveItemRepository.saveAll(liveItems);

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

    @Transactional(readOnly = true)
    public List<LiveMainDto> getMainLiveList() {
        List<LiveMainDto> result = new ArrayList<>();

        // 1. 현재 진행 중인 라이브
        Live ongoingLive = liveRepository
                .findFirstByLiveStatusOrderByLiveStartTimeAsc(LiveStatus.ONGOING)
                .orElse(null);

        if (ongoingLive != null) {
            result.add(new LiveMainDto(ongoingLive.getId(), ongoingLive.getTitle(), ongoingLive.getImageUrl()));
        } else {
            // 2. 예정된 라이브
            Live scheduledLive = liveRepository
                    .findFirstByLiveStatusAndLiveStartTimeAfterOrderByLiveStartTimeAsc(
                            LiveStatus.SCHEDULED, LocalDateTime.now())
                    .orElse(null);

            if (scheduledLive != null) {
                result.add(new LiveMainDto(scheduledLive.getId(), scheduledLive.getTitle(), scheduledLive.getImageUrl()));
            }
        }

        // 3. 종료된 라이브들
        int remaining = 5 - result.size();
        List<Live> endedLives = liveRepository
                .findTopNByLiveStatusOrderByLiveEndTimeDesc(LiveStatus.COMPLETED, remaining);

        for(Live live : endedLives) {
            result.add(new LiveMainDto(live.getId(), live.getTitle(), live.getImageUrl()));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<LiveScheduledDto> getLiveScheduledByDate(LocalDate date) {
        List<Live> lives = liveRepository.findAllByLiveDateOrderByLiveStartTimeAsc(date);

        return lives.stream()
                .map(live -> new LiveScheduledDto(
                        live.getId(),
                        live.getTitle(),
                        live.getUser().getBrand().getBrandName(),
                        live.getImageUrl(),
                        live.getLiveStartTime(),
                        live.getLiveDate(),
                        live.getLiveStatus(),
                        live.getLiveItems().stream()
                                .map(item -> new LiveItemResponseDto(
                                        item.getItem().getItemId(),
                                        item.getItem().getItemName(),
                                        item.getItem().getItemImages().get(0).getUrl(),
                                        item.getItem().getPrice()
                                ))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    public Optional<LiveMainDto> getLiveOngoingByBrandId(Long brandId) {
        // 1. 브랜드 ID로 유저 ID 찾기
        Optional<User> user = userRepository.findByBrandBrandId(brandId);

        if(user.isPresent()) {
            // 2. 유저 ID로 진행 중인 라이브 방송 찾기
            Optional<Live> live = liveRepository.findFirstByUserIdAndLiveStatus(user.get().getId(), LiveStatus.ONGOING);

            if(live.isPresent()) {
                Live ongoingLive = live.get();
                LiveMainDto liveMainDto = new LiveMainDto(
                        ongoingLive.getId(),
                        ongoingLive.getTitle(),
                        ongoingLive.getImageUrl()
                );
                return Optional.of(liveMainDto);
            }
        }
        return Optional.empty();
    }


}