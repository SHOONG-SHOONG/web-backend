package shoong.web_backend.domain.item.service;// example

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shoong.web_backend.domain.brand.entity.Brand;
import shoong.web_backend.domain.brand.repository.BrandRepository;
import shoong.web_backend.domain.item.dto.ItemRequestDto;
import shoong.web_backend.domain.item.entity.Item;
import shoong.web_backend.domain.item.repository.ItemRepository;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final BrandRepository brandRepository;

    public void createItem(ItemRequestDto dto) {
        Brand brand = brandRepository.findById(dto.getBrandId())
                .orElseThrow(() -> new RuntimeException("Brand not found"));

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
                .build();

        itemRepository.save(item);
    }
}