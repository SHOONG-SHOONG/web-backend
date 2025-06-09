package shoong.web_backend.domain.live.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import shoong.web_backend.aws.AmazonS3Manager;
import shoong.web_backend.domain.item.entity.Item;
import shoong.web_backend.domain.item.repository.ItemRepository;
import shoong.web_backend.domain.live.dto.*;
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
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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

        // 기본 이미지 URL
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
                .liveDate(Optional.ofNullable(liveCreateRequestDto.getLiveDate()).orElse(LocalDate.now()))
                .liveStartTime(liveCreateRequestDto.getLiveStartTime())
                .streamKey(liveCreateRequestDto.getStreamKey())
                .liveEndTime(liveCreateRequestDto.getLiveStartTime().plusHours(1))
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
    public LiveMainDto getOngoingLiveMainDto() {
        Live live = liveRepository.findFirstByLiveStatusOrderByLiveStartTimeAsc(LiveStatus.ONGOING)
                .orElseThrow(() -> new IllegalStateException("진행 중인 라이브가 없습니다."));

        // LiveItem은 여러 개일 수 있으므로 하나만 선택
        LiveItem firstItem = live.getLiveItems().isEmpty() ? null : live.getLiveItems().get(0);

        return LiveMainDto.builder()
                .liveId(live.getId())
                .itemId(firstItem != null ? firstItem.getItem().getItemId() : null)
                .title(live.getTitle())
                .imageUrl(live.getImageUrl())
                .status(live.getLiveStatus())
                .itemName(firstItem != null ? firstItem.getItem().getItemName() : null)
                .price(firstItem != null ? firstItem.getItem().getPrice() : null)
                .discountRate(firstItem != null ? firstItem.getItem().getDiscountRate() : null)
                .itemImageUrl(firstItem != null ? firstItem.getItem().getItemImages().get(0).getUrl() : null)
                .liveDate(firstItem != null ? firstItem.getLive().getLiveDate() : null)
                .liveStartTime(firstItem != null ? firstItem.getLive().getLiveStartTime() : null)
                .liveEndTime(firstItem != null ? firstItem.getLive().getLiveEndTime() : null)
                .build();
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
                                        item.getItem().getPrice(),
                                        item.getItem().getDiscountRate()
                                ))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    public Optional<LiveMainDto> getLiveOngoingByBrandId(Long brandId) {
        return userRepository.findByBrandBrandId(brandId)
                .flatMap(user -> liveRepository.findFirstByUserIdAndLiveStatus(user.getId(), LiveStatus.ONGOING))
                .flatMap(live -> {
                    Optional<LiveItem> firstLiveItem = liveItemRepository.findFirstByLiveIdOrderByIdAsc(live.getId());
                    Item item = firstLiveItem.map(LiveItem::getItem).orElse(null);

                    if (item != null && item.getItemImages() != null && !item.getItemImages().isEmpty()) {
                        return Optional.of(new LiveMainDto(
                                live.getId(),
                                item.getItemId(),
                                live.getTitle(),
                                live.getImageUrl(),
                                item.getItemName(),
                                item.getPrice(),
                                item.getDiscountRate(),
                                item.getItemImages().get(0).getUrl(),
                                live.getLiveStatus(),
                                live.getLiveDate(),
                                live.getLiveStartTime(),
                                live.getLiveEndTime()
                        ));
                    } else {
                        return Optional.empty(); // 아이템이 없거나 이미지가 없으면 반환하지 않음
                    }
                });
    }

    @Transactional(readOnly = true)
    public List<LiveMainDto> getAllLivesByUser(User user) {
        List<Live> lives = liveRepository.findAllByUserIdOrderByLiveStartTimeDesc(user.getId());

        return lives.stream()
                .map(live -> {
                    Optional<LiveItem> firstLiveItem = liveItemRepository.findFirstByLiveIdOrderByIdAsc(live.getId());
                    Item item = firstLiveItem.map(LiveItem::getItem).orElse(null);

                    if (item != null && item.getItemImages() != null && !item.getItemImages().isEmpty()) {
                        return new LiveMainDto(
                                live.getId(),
                                item.getItemId(),
                                live.getTitle(),
                                live.getImageUrl(),
                                item.getItemName(),
                                item.getPrice(),
                                item.getDiscountRate(),
                                item.getItemImages().get(0).getUrl(),
                                live.getLiveStatus(),
                                live.getLiveDate(),
                                live.getLiveStartTime(),
                                live.getLiveEndTime()
                        );
                    } else {
                        // 아이템이 없거나 이미지가 없으면 기본값 처리
                        return new LiveMainDto(
                                live.getId(),
                                item.getItemId(),
                                live.getTitle(),
                                live.getImageUrl(),
                                null, null, null, null, live.getLiveStatus(),
                                live.getLiveDate(),
                                live.getLiveStartTime(),
                                live.getLiveEndTime()
                        );
                    }
                })
                .collect(Collectors.toList());
    }

    public String searchStreamKeyByTitle(String streamingTitle) {
        return liveRepository
                .findFirstByTitleContainingIgnoreCaseOrderByLiveDateDesc(streamingTitle)
                .map(Live::getStreamKey)
                .orElseThrow(() -> new NoSuchElementException("해당 제목을 포함하는 방송이 존재하지 않습니다."));
    }

    public String getLatestStreamKey() {
        return liveRepository
                .findFirstByOrderByLiveStartTimeDesc()
                .map(Live::getStreamKey)
                .orElseThrow(() -> new NoSuchElementException("가장 최근 방송이 존재하지 않습니다."));

    }

    @Transactional(readOnly = true)
    public List<LiveMainDto> getCompLiveList() {
        List<LiveMainDto> result = new ArrayList<>();

        // 완료된 라이브 중 최근 4개 가져오기
        List<Live> completedLives = liveRepository
                .findTopNByLiveStatusOrderByLiveEndTimeDesc(LiveStatus.COMPLETED, 4);

        for (Live live : completedLives) {
            // 첫 번째 관련 LiveItem 가져오기
            Optional<LiveItem> firstLiveItemOpt = liveItemRepository
                    .findFirstByLiveIdOrderByIdAsc(live.getId());

            // 기본값 처리
            Long itemId = null;
            String itemName = null;
            Long price = null;
            Double discountRate = null;
            String itemImageUrl = null;

            if (firstLiveItemOpt.isPresent()) {
                Item item = firstLiveItemOpt.get().getItem();
                if (item != null) {
                    itemId = item.getItemId();
                    itemName = item.getItemName();
                    price = item.getPrice();
                    discountRate = item.getDiscountRate();

                    if (item.getItemImages() != null && !item.getItemImages().isEmpty()) {
                        itemImageUrl = item.getItemImages().get(0).getUrl();
                    }
                }
            }

            // DTO 생성 및 추가
            result.add(new LiveMainDto(
                    live.getId(),
                    itemId,
                    live.getTitle(),
                    live.getImageUrl(),
                    itemName,
                    price,
                    discountRate,
                    itemImageUrl,
                    live.getLiveStatus(),
                    live.getLiveDate(),
                    live.getLiveStartTime(),
                    live.getLiveEndTime()
            ));
        }

        return result;

    }

    @Transactional
    public void updateReplayUrlByStreamKey(String streamKey, String vodUrl) {
        Live live = liveRepository.findTopByStreamKeyOrderByLiveStartTimeDesc(streamKey)
                .orElseThrow(() -> new NoSuchElementException("해당 streamKey로 된 라이브가 존재하지 않습니다."));
        // 다시보기 url저장
        live.setReplayURL(vodUrl);
        liveRepository.save(live);
    }

    @Transactional
    (readOnly = true)
    public LiveDetailDto getLiveById(Long liveid) {
        Live live = liveRepository.findById(liveid)
                .orElseThrow(() -> new NoSuchElementException("해당 라이브가 존재하지 않습니다."));

        List<LiveItemResponseDto> liveItemResponseDtos = live.getLiveItems().stream()
                .map(liveItem -> {
                    Item item = liveItem.getItem();
                    return new LiveItemResponseDto(
                            item.getItemId(),
                            item.getItemName(),
                            item.getItemImages().get(0).getUrl(),
                            item.getPrice(),
                            item.getDiscountRate()
                    );
                })
                .collect(Collectors.toList());

        return new LiveDetailDto(
                live.getId(),
                live.getTitle(),
                live.getDescription(),
                live.getImageUrl(),
                live.getStreamKey(),
                live.getLiveStartTime(),
                live.getLiveEndTime(),
                live.getLiveDate(),
                live.getLiveStatus(),
                live.getReplayURL(),
                liveItemResponseDtos
        );
    }

    @Transactional
    public void completeLive(Long liveId) {
        Live live = liveRepository.findById(liveId)
                .orElseThrow(() -> new NoSuchElementException("해당 라이브가 존재하지 않습니다. liveId: " + liveId));

        live.setLiveStatus(LiveStatus.COMPLETED);
        live.setLiveEndTime(LocalDateTime.now()); 

        liveRepository.save(live);
    }

    @Scheduled(fixedRate = 60_000) // 1분마다 실행
    @Transactional
    public void autoUpdateLiveStatus() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        List<Live> lives = liveRepository.findAll(); // 조건이 명확하므로 전체 조회해도 부담 작음

        for (Live live : lives) {
            LocalDateTime start = live.getLiveStartTime();
            LocalDateTime end = live.getLiveEndTime();

            if (end != null && end.isBefore(now)) {
                live.setLiveStatus(LiveStatus.COMPLETED);
            } else if (start != null && start.isBefore(now)) {
                live.setLiveStatus(LiveStatus.ONGOING);
            } else {
                live.setLiveStatus(LiveStatus.SCHEDULED);
            }
        }
    }
}
