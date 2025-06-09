package shoong.web_backend.domain.item.service;

import jakarta.validation.Valid;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import shoong.web_backend.domain.brand.entity.Brand;
import shoong.web_backend.domain.item.condition.ItemSearchCondition;
import shoong.web_backend.domain.item.dto.ItemRequestDto;
import shoong.web_backend.domain.item.dto.ItemResponseDto;
import shoong.web_backend.domain.item.dto.ItemUpdateRequestDto;
import shoong.web_backend.domain.item.entity.Item;
import shoong.web_backend.domain.item.enums.ItemStatus;
import shoong.web_backend.domain.item.repository.ItemRepository;
import shoong.web_backend.domain.item_image.entity.ItemImage;
import shoong.web_backend.domain.item_image.service.ItemImageService;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.enums.UserRole;
import shoong.web_backend.domain.user.repository.UserRepository;
import shoong.web_backend.exception.NotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemImageService itemImageService;

    // 아이템 생성
    @Transactional
    public void createItem(ItemRequestDto dto, User user, MultipartFile[] imageFiles) {
        Brand brand = findBrandByUser(user);
        Item item = createItemFromDto(dto, brand);

        saveItemWithImages(item, imageFiles);
    }
    // 판매자의 상품 목록
    @Transactional(readOnly = true)
    public List<ItemResponseDto> getAdminItemList(Long userId) {
        User user = findUserById(userId);
        if(user.getRole() != UserRole.CLIENT){
            Brand brand = findBrandByUser(user);
            List<Item> items = itemRepository.findByBrand_BrandId(brand.getBrandId());
            return items.stream()
                    .map(this::convertToItemResponseDto)
                    .collect(Collectors.toList()); // ✅ 여기를 수정
        }
        return null;
    }


    // 아이템 상세 조회
    @Transactional(readOnly = true)
    public ItemResponseDto getItem(Long itemId) {
        Item item = findItemById(itemId);
        MDC.put("eventType", "item_detail");
        MDC.put("itemId", String.valueOf(itemId));
        log.info("상품 조회");

        MDC.clear();
        return convertToItemResponseDto(item);
    }

    // 아이템 검색 (동적 쿼리)
    @Transactional(readOnly = true)
    public Page<ItemResponseDto> searchItems(ItemSearchCondition condition, Pageable pageable) {
        return itemRepository.searchItems(condition, pageable);
    }

    // 아이템 삭제 (상태 변경)
    @Transactional
    public void deleteItem(Long itemId) {
        Item item = findItemById(itemId);
        validateItemNotDeleted(item);
        markItemAsDeleted(item);
    }

    // 아이템 업데이트
    @Transactional
    public void updateItem(Long itemId, @Valid ItemUpdateRequestDto updateDto, Long userId) {
        validateUserAuthorization(userId);
        Item item = findItemById(itemId);
        applyItemUpdates(item, updateDto);
        itemRepository.save(item);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("유저 조회 실패"));
    }

    private void validateUserAuthorization(Long userId) {
        User user = findUserById(userId);
        if (user.getRole().equals(UserRole.CLIENT)) {
            throw new AuthorizationDeniedException("권한이 없는 유저(클라이언트)로부터의 상품 업데이트 요청");
        }
    }

    private Brand findBrandByUser(User user) {
        return Optional.ofNullable(user.getBrand())
                .orElseThrow(() -> new NotFoundException("브랜드 조회 실패"));
    }

    private Item findItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("해당 상품이 존재하지 않습니다."));
    }

    private Item createItemFromDto(ItemRequestDto dto, Brand brand) {
        return Item.builder()
                .brand(brand)
                .itemName(dto.getItemName())
                .price(dto.getPrice())
                .discountRate(dto.getDiscountRate())
                .description(dto.getDescription())
                .itemQuantity(dto.getItemQuantity())
                .category(dto.getCategory())
                .createdAt(dto.getCreatedAt())
                .discountExpiredAt(dto.getDiscountExpiredAt())
                .status(ItemStatus.PENDING)
                .build();
    }

    private void saveItemWithImages(Item item, MultipartFile[] imageFiles) {
        itemRepository.save(item);

        List<ItemImage> savedImages = itemImageService.saveMultiImagesAndReturnList(imageFiles, item);

        item.getItemImages().addAll(savedImages);

        itemRepository.save(item);
    }

    private void validateItemNotDeleted(Item item) {
        if (item.getStatus() == ItemStatus.DELETED) {
            throw new IllegalStateException("이미 삭제된 상품입니다.");
        }
    }

    private void markItemAsDeleted(Item item) {
        item.setStatus(ItemStatus.DELETED);
    }

    private void applyItemUpdates(Item item, ItemUpdateRequestDto updateDto) {
        if (updateDto.getItemName() != null) {
            item.setItemName(updateDto.getItemName());
        }
        if (updateDto.getPrice() != null) {
            item.setPrice(updateDto.getPrice());
        }
        if (updateDto.getDiscountRate() != null) {
            item.setDiscountRate(updateDto.getDiscountRate());
        }
        if (updateDto.getDescription() != null) {
            item.setDescription(updateDto.getDescription());
        }
        if (updateDto.getItemQuantity() != null) {
            item.setItemQuantity(updateDto.getItemQuantity());
        }
        if (updateDto.getCategory() != null) {
            item.setCategory(updateDto.getCategory());
        }
    }

    // ===== DTO 변환 헬퍼 메서드 =====
    private ItemResponseDto convertToItemResponseDto(Item item) {
        return ItemResponseDto.builder()
                .itemId(item.getItemId())
                .brandId(item.getBrand().getBrandId())
                .itemName(item.getItemName())
                .price(item.getPrice())
                .discountRate(item.getDiscountRate() * 100)
                .finalPrice(calculateFinalPrice(item))
                .wishlistCount(item.getWishlists().size())
                .description(item.getDescription())
                .itemQuantity(item.getItemQuantity())
                .category(item.getCategory())
                .discountExpiredAt(item.getDiscountExpiredAt())
                .status(item.getStatus())
                .itemImages(item.getItemImages())
                .build();
    }

    private int calculateFinalPrice(Item item) {
        return (int) Math.round(item.getPrice() * (1.0 - item.getDiscountRate()));
    }
}
