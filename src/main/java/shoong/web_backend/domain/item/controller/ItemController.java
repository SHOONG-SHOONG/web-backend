package shoong.web_backend.domain.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shoong.web_backend.domain.brand.repository.BrandRepository;
import shoong.web_backend.domain.item.dto.ItemRequestDto;
import shoong.web_backend.domain.item.repository.ItemRepository;
import shoong.web_backend.domain.item.service.ItemService;

@RestController
@RequestMapping("/item")
@RequiredArgsConstructor
public class ItemController {

    private final ItemRepository itemRepository;
    private final BrandRepository brandRepository;
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<Void> createItem(@RequestBody ItemRequestDto requestDto){
        itemService.createItem(requestDto);
        return ResponseEntity.ok().build();
    }
}
