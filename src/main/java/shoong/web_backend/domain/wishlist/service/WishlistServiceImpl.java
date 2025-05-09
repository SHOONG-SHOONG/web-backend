package shoong.web_backend.domain.wishlist.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shoong.web_backend.domain.item.entity.Item;
import shoong.web_backend.domain.item.repository.ItemRepository;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.repository.UserRepository;
import shoong.web_backend.domain.wishlist.dto.WishlistResponseDTO;
import shoong.web_backend.domain.wishlist.entity.Wishlist;
import shoong.web_backend.domain.wishlist.repository.WishlistRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ItemRepository itemRepository;


    @Override
    public List<WishlistResponseDTO> getWishlist(User user) {
        List<Wishlist> wishlists = wishlistRepository.findAllByUserId(user.getId());
        return wishlists.stream()
                .map(WishlistResponseDTO::from)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public boolean toggleWishlist(User user, Long itemId) {

        Optional<Wishlist> wishlistOpt = wishlistRepository.findByUserIdAndItemItemId(user.getId(), itemId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));

        if(wishlistOpt.isPresent()) {
            wishlistRepository.delete(wishlistOpt.get());
            return false;
        } else {
            Wishlist wishlist = new Wishlist();
            wishlist.setUser(user);
            wishlist.setItem(item);
            wishlistRepository.save(wishlist);
            return true;
        }

    }

}
