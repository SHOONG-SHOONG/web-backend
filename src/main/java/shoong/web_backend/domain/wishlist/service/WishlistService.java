package shoong.web_backend.domain.wishlist.service;

import shoong.web_backend.domain.wishlist.dto.WishlistResponseDTO;
import java.util.List;

public interface WishlistService {
    List<WishlistResponseDTO> getWishlistByUserId(Long userId);
    void addWishlistItem(Long userId, Long itemId);
    void deleteWishlistItem(Long wishlistId);
}
