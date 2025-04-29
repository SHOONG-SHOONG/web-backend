package shoong.web_backend.domain.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shoong.web_backend.domain.brand.entity.Brand;
import shoong.web_backend.domain.brand.repository.BrandRepository;
import shoong.web_backend.domain.item.entity.Item;
import shoong.web_backend.domain.item.repository.ItemRepository;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/item")
@RequiredArgsConstructor
public class ItemController {

    private final ItemRepository itemRepository;
    private final BrandRepository brandRepository;

    @PostMapping("/test-insert")
    public ResponseEntity<Void> insertTestItem() {
        Item item = new Item();

        Brand brand = brandRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Brand not found"));

        item.setBrand(brand);

        // 기본 정보 셋팅
        item.setItemName("Test Item");
        item.setPrice(10000L);
        item.setDiscountRate(0.1);
        item.setDescription("This is a test item");
        item.setItemQuantity(100);
        item.setCategory("Electronics");
        item.setCreatedAt(LocalDateTime.now());
        item.setDiscountExpiredAt(LocalDateTime.now().plusDays(7));
        item.setStatus(1);

        itemRepository.save(item);
        return ResponseEntity.ok().build();
    }
}
