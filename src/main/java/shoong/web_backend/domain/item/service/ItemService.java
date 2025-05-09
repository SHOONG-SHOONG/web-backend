package shoong.web_backend.domain.item.service;// example

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;
import shoong.web_backend.domain.brand.entity.Brand;
import shoong.web_backend.domain.brand.repository.BrandRepository;
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
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemImageService itemImageService;

    // 아이템 생성
    @Transactional
    public void createItem(ItemRequestDto dto, Long userId, MultipartFile[] imageFiles) {
        User user = findUserById(userId);
        Brand brand = findBrandByUser(user);
        Item item = createItemFromDto(dto, brand);

        saveItemWithImages(item, imageFiles);
    }

    // 아이템 상세 조회
    @Transactional(readOnly = true)
    public ItemResponseDto getItem(Long itemId) {
        Item item = findItemById(itemId);
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

    // ===== 사용자 관련 헬퍼 메서드 =====
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

    // ===== 브랜드 관련 헬퍼 메서드 =====
    private Brand findBrandByUser(User user) {
        return Optional.ofNullable(user.getBrand())
                .orElseThrow(() -> new NotFoundException("브랜드 조회 실패"));
    }

    // ===== 아이템 관련 헬퍼 메서드 =====
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
                .status(ItemStatus.ON_SALE)
                .build();
    }

    private void saveItemWithImages(Item item, MultipartFile[] imageFiles) {
        // 먼저 item을 저장 (ID 생성을 위해)
        itemRepository.save(item);

        // 이미지 처리 및 반환
        List<ItemImage> savedImages = itemImageService.saveMultiImagesAndReturnList(imageFiles, item);

        // 저장된 이미지들을 item의 itemImages 컬렉션에 추가
        item.getItemImages().addAll(savedImages);

        // 변경사항 저장
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
                // 30%와 같이 처리를 위해 0.3이라면 30퍼센트의 할인율을 갖추게함.
                .discountRate(item.getDiscountRate() * 100)
                // finalPrice -> 할인 적용된 최종 가격, 소수점 이하는 제거
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
        // 1.0 - 0.3(할인율) -> 0.7 * 가격
        return (int) Math.floor(item.getPrice() * (1.0 - item.getDiscountRate()));
    }
}

//@Service
//@RequiredArgsConstructor
//public class ItemService {
//
//    private final ItemRepository itemRepository;
//    private final BrandRepository brandRepository;
//    private final UserRepository userRepository;
//    private final ItemImageService itemImageService;
//    // 아이템을 생성할 때 아이템 이미지도 함께 저장
//    @Transactional
//    public void createItem(ItemRequestDto dto, Long userId, MultipartFile[] imageFiles) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("유저 조회 실패"));
//
//        Brand brand = Optional.ofNullable(user.getBrand())
//                .orElseThrow(() -> new RuntimeException("브랜드 조회 실패"));
//
//        Item item = Item.builder()
//                .brand(brand)
//                .itemName(dto.getItemName())
//                .price(dto.getPrice())
//                .discountRate(dto.getDiscountRate())
//                .description(dto.getDescription())
//                .itemQuantity(dto.getItemQuantity())
//                .category(dto.getCategory())
//                .createdAt(dto.getCreatedAt())
//                .discountExpiredAt(dto.getDiscountExpiredAt())
//                .status(ItemStatus.ON_SALE)
//                .build();
//
//        // 먼저 item을 저장 (ID 생성을 위해)
//        itemRepository.save(item);
//
//        // 이미지 처리 및 item.itemImages 컬렉션 업데이트
//        List<ItemImage> savedImages = itemImageService.saveMultiImagesAndReturnList(imageFiles, item);
//
//        // 저장된 이미지들을 item의 itemImages 컬렉션에 추가
//        // (ItemImageService에서 이미 설정했다면 이 단계는 생략 가능)
//        item.getItemImages().addAll(savedImages);
//
//        // 변경사항 저장 (트랜잭션 내에서 자동으로 반영되지만, 명시적으로 호출)
//        itemRepository.save(item);
//    }
//
//    @Transactional(readOnly = true)
//    public ItemResponseDto getItem(Long itemId){
//        Item item = itemRepository.findById(itemId)
//                .orElseThrow(() -> new RuntimeException("상품 조회 실패"));
//        ItemResponseDto itemResponseDto = ItemResponseDto.builder()
//                .itemId(item.getItemId())
//                .brandId(item.getBrand().getBrandId())
//                .itemName(item.getItemName())
//                .price(item.getPrice())
//                // 30%와 같이 처리를 위해 0.3이라면 30퍼센트의 할인율을 갖추게함.
//                .discountRate(item.getDiscountRate() * 100)
//                // finalPrice -> 할인 적용된 최종 가격
//                // 소수점 이하는 제거
//                // 1.0 - 0.3(할인율) -> 0.7 * 가격
//                .finalPrice((int) Math.floor(item.getPrice() * (1.0 - item.getDiscountRate())))
//                .wishlistCount(item.getWishlists().size())
//                .description(item.getDescription())
//                .itemQuantity(item.getItemQuantity())
//                .category(item.getCategory())
//                .discountExpiredAt(item.getDiscountExpiredAt())
//                .status(item.getStatus()) // Enum 값을 사용
//                .itemImages(item.getItemImages())
//                .build();
//        return itemResponseDto;
//    }
//
//    // 동적 쿼리 부분
//    @Transactional(readOnly = true)
//    public Page<ItemResponseDto> searchItems(ItemSearchCondition condition, Pageable pageable) {
//        return itemRepository.searchItems(condition, pageable);
//    }
//
//    @Transactional
//    public void deleteItem(Long itemId) {
//        Item item = itemRepository.findById(itemId)
//                        .orElseThrow(() -> new RuntimeException("상품 조회 실패"));
//
//        if (item.getStatus() == ItemStatus.DELETED){
//            throw new RuntimeException("이미 삭제된 상품입니다."); // 재삭제 방지
//        }
//
//        item.setStatus(ItemStatus.DELETED);
//    }
//    @Transactional
//    public void updateItem(Long itemId, @Valid ItemUpdateRequestDto updateDto, Long userId) {
//        Item item = itemRepository.findById(itemId)
//                .orElseThrow(() -> new NotFoundException("해당 상품이 존재하지 않습니다."));
//        User user = userRepository.findById(userId).orElseThrow(()->new NotFoundException("유저 조회 실패"));
//
//        if(user.getRole().equals(UserRole.CLIENT)){
//            throw new AuthorizationDeniedException("권한이 없는 유저(클라이언트)로부터의 상품 업데이트 요청");
//        }
//        if(updateDto.getItemName() != null){
//            item.setItemName(updateDto.getItemName());
//        }
//        if(updateDto.getPrice() != null){
//            item.setPrice(updateDto.getPrice());
//        }
//        if(updateDto.getDiscountRate() != null){
//            item.setDiscountRate(updateDto.getDiscountRate());
//        }
//        if(updateDto.getDescription() != null){
//            item.setDescription(updateDto.getDescription());
//        }
//        if(updateDto.getItemQuantity() != null){
//            item.setItemQuantity(updateDto.getItemQuantity());
//        }
//        if(updateDto.getCategory() != null){
//            item.setCategory(updateDto.getCategory());
//        }
//        itemRepository.save(item);
//    }
//}