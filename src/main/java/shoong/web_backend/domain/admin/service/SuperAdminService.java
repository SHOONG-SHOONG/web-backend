package shoong.web_backend.domain.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shoong.web_backend.domain.admin.dto.AdminItemDto;
import shoong.web_backend.domain.admin.dto.AdminUserDto;
import shoong.web_backend.domain.item.entity.Item;
import shoong.web_backend.domain.item.enums.ItemStatus;
import shoong.web_backend.domain.item.repository.ItemRepository;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.enums.UserRole;
import shoong.web_backend.domain.user.enums.UserStatus;
import shoong.web_backend.domain.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuperAdminService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    // PENDING 상태인 스트리머 리스트 조회
    public List<AdminUserDto> getPendingUsers() {
        List<User> users = userRepository.findByRoleAndUserStatus(UserRole.STREAMER, UserStatus.PENDING);
        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // 스트리머 승인
    public void approveStreamer(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        if (user.getRole() != UserRole.STREAMER || user.getUserStatus() != UserStatus.PENDING) {
            throw new IllegalStateException("승인 대상이 아닙니다.");
        }

        user.setUserStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    // 스트리머 거절
    public void rejectStreamer(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        if (user.getRole() != UserRole.STREAMER || user.getUserStatus() != UserStatus.PENDING) {
            throw new IllegalStateException("거절 대상이 아닙니다.");
        }

        user.setUserStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    // PENDING 상태인 아이템 리스트 조회
    public List<AdminItemDto> getPendingItem() {
        List<Item> items = itemRepository.findByStatus(ItemStatus.PENDING);
        return items.stream()
                .map(this::toAdminItemDto)
                .collect(Collectors.toList());
    }

    // 아이템 승인
    public void approveItems(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이템이 존재하지 않습니다."));

        if (item.getStatus() != ItemStatus.PENDING) {
            throw new IllegalStateException("승인 대상이 아닙니다.");
        }

        item.setStatus(ItemStatus.ON_SALE);
        itemRepository.save(item);
    }

    // 아이템 거절
    public void rejectItems(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이템이 존재하지 않습니다."));

        if (item.getStatus() != ItemStatus.PENDING) {
            throw new IllegalStateException("거절 대상이 아닙니다.");
        }

        item.setStatus(ItemStatus.INACTIVE);
        itemRepository.save(item);
    }

    private AdminUserDto toDto(User user) {
        return AdminUserDto.builder()
                .id(user.getId())
                .userAlias(user.getUserAlias())
                .userEmail(user.getUserEmail())
                .userName(user.getUserName())
                .name(user.getName())
                .userPhone(user.getUserPhone())
                .birthDay(user.getBirthDay())
                .role(user.getRole())
                .registrationNumber(user.getRegistrationNumber())
                .userStatus(user.getUserStatus())
                .userAddress(user.getUserAddress())
                .brandId(user.getBrand() != null ? user.getBrand().getBrandId() : null)
                .brandName(user.getBrand().getBrandName())
                .build();
    }

    private AdminItemDto toAdminItemDto(Item item) {
        return AdminItemDto.builder()
                .itemId(item.getItemId())
                .itemName(item.getItemName())
                .price(item.getPrice())
                .discountRate(item.getDiscountRate())
                .description(item.getDescription())
                .itemQuantity(item.getItemQuantity())
                .category(item.getCategory())
                .createdAt(item.getCreatedAt())
                .discountExpiredAt(item.getDiscountExpiredAt())
                .status(item.getStatus())
                .brandId(item.getBrand() != null ? item.getBrand().getBrandId() : null)
                .brandName(item.getBrand().getBrandName())
                .build();
    }
}
