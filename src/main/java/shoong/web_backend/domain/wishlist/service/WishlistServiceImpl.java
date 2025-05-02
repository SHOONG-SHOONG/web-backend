package shoong.web_backend.domain.wishlist.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shoong.web_backend.domain.item.entity.Item;
import shoong.web_backend.domain.item.repository.ItemRepository;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.repository.UserRepository;
import shoong.web_backend.domain.wishlist.dto.WishlistResponseDTO;
import shoong.web_backend.domain.wishlist.entity.Wishlist;
import shoong.web_backend.domain.wishlist.repository.WishlistRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;


    @Override
    public List<WishlistResponseDTO> getWishlistByUserId(Long userId) {
        List<Wishlist> wishlists = wishlistRepository.findAllByUserId(userId);
        return wishlists.stream()
                .map(WishlistResponseDTO::from)
                .collect(Collectors.toList());
    }


    @Override
    public void addWishlistItem(Long userId, Long itemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));

        // 중복 체크
        boolean exists = wishlistRepository.existsByUserAndItem(user, item);
        if (exists) {
            throw new IllegalStateException("Item already exists in wishlist");
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setItem(item);

        wishlistRepository.save(wishlist);
    }

    @Override
    public void deleteWishlistItem(Long wishlistId) {
        if (!wishlistRepository.existsById(wishlistId)) {
            throw new EntityNotFoundException("Wishlist item not found with ID: " + wishlistId);
        }
        wishlistRepository.deleteById(wishlistId);
    }
}
