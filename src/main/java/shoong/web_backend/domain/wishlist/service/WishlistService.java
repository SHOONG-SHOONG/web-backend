package shoong.web_backend.domain.wishlist.service;

import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.wishlist.dto.WishlistResponseDTO;
import java.util.List;

public interface WishlistService {
    List<WishlistResponseDTO> getWishlist(User user);
    boolean toggleWishlist(User user, Long itemId);
}
