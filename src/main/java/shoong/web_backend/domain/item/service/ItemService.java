package shoong.web_backend.domain.item.service;// example

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shoong.web_backend.domain.brand.entity.Brand;
import shoong.web_backend.domain.brand.repository.BrandRepository;
import shoong.web_backend.domain.item.condition.ItemSearchCondition;
import shoong.web_backend.domain.item.dto.ItemRequestDto;
import shoong.web_backend.domain.item.dto.ItemResponseDto;
import shoong.web_backend.domain.item.entity.Item;
import shoong.web_backend.domain.item.enums.ItemStatus;
import shoong.web_backend.domain.item.repository.ItemRepository;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final BrandRepository brandRepository;
    private final UserRepository userRepository;
    @Transactional
    public void createItem(ItemRequestDto dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 조회 실패"));

        Brand brand = Optional.ofNullable(user.getBrand())
                .orElseThrow(() -> new RuntimeException("브랜드 조회 실패"));

        Item item = Item.builder()
                .brand(brand)
                .itemName(dto.getItemName())
                .price(dto.getPrice())
                .discountRate(dto.getDiscountRate())
                .description(dto.getDescription())
                .itemQuantity(dto.getItemQuantity())
                .category(dto.getCategory())
                .createdAt(dto.getCreatedAt())
                .discountExpiredAt(dto.getDiscountExpiredAt())
            //    .itemImages()
                .status(ItemStatus.ON_SALE)
                .build();

        itemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public ItemResponseDto getItem(Long itemId){
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("상품 조회 실패"));
        ItemResponseDto itemResponseDto = ItemResponseDto.builder()
                .itemId(item.getItemId())
                .brandId(item.getBrand().getBrandId())
                .itemName(item.getItemName())
                .price(item.getPrice())
                // 30%와 같이 처리를 위해 0.3이라면 30퍼센트의 할인율을 갖추게함.
                .discountRate(item.getDiscountRate() * 100)
                // finalPrice -> 할인 적용된 최종 가격
                // 소수점 이하는 제거
                // 1.0 - 0.3(할인율) -> 0.7 * 가격
                .finalPrice((int) Math.floor(item.getPrice() * (1.0 - item.getDiscountRate())))
                .wishlistCount(item.getWishlists().size())
                .description(item.getDescription())
                .itemQuantity(item.getItemQuantity())
                .category(item.getCategory())
                .discountExpiredAt(item.getDiscountExpiredAt())
                .status(item.getStatus()) // Enum 값을 사용
                .itemImages(item.getItemImages())
                .build();
        return itemResponseDto;
    }

//    @Transactional(readOnly = true)
//    public List<ItemResponseDto> getItemsByCategory(String category){
//        List<ItemResponseDto> itemResponseDtos = itemRepository.findByCategory(category).stream()
//                .map(item -> ItemResponseDto.builder()
//                        .itemId(item.getItemId())
//                        .brandId(item.getBrand().getBrandId())
//                        .itemName(item.getItemName())
//                        .price(item.getPrice())
//                        .discountRate(item.getDiscountRate() * 100) // 0.1 -> 10%
//                        .finalPrice((int) (item.getPrice() * (1.0 - item.getDiscountRate())))
//                        .wishlistCount(item.getWishlists().size())
//                        .description(item.getDescription())
//                        .itemQuantity(item.getItemQuantity())
//                        .category(item.getCategory())
//                        .discountExpiredAt(item.getDiscountExpiredAt())
//                        .status(item.getStatus())
//                        .itemImages(item.getItemImages())
//                        .build())
//                .collect(Collectors.toList());
//        return itemResponseDtos;
//    }

    // 동적 쿼리 부분
    @Transactional(readOnly = true)
    public Page<ItemResponseDto> searchItems(ItemSearchCondition condition, Pageable pageable) {
        return itemRepository.searchItems(condition, pageable);
    }

    @Transactional
    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                        .orElseThrow(() -> new RuntimeException("상품 조회 실패"));

        if (item.getStatus() == ItemStatus.DELETED){
            throw new RuntimeException("이미 삭제된 상품입니다."); // 재삭제 방지
        }

        item.setStatus(ItemStatus.DELETED);
    }
}