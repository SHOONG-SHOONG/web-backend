package shoong.web_backend.domain.wishlist.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import shoong.web_backend.domain.cart.dto.CartResponseDto;
import shoong.web_backend.domain.cart.entity.Cart;
import shoong.web_backend.domain.wishlist.entity.Wishlist;

@Getter
@Setter
@Builder
public class WishlistResponseDTO {
    private Long wishlistId;
    private Long itemId;
    private String itemName;
    private Long price;
    private double discountRate;
    private int finalPrice;

    public static WishlistResponseDTO from(Wishlist wishlist) {
        Long price = wishlist.getItem().getPrice();
        double discountRate = wishlist.getItem().getDiscountRate();
        int finalPrice = (int) (price * (1 - discountRate));

        return WishlistResponseDTO.builder()
                .wishlistId(wishlist.getId())
                .itemId(wishlist.getItem().getItemId())
                .itemName(wishlist.getItem().getItemName())
                .price(price)
                .discountRate(discountRate)
                .finalPrice(finalPrice)
                .build();
    }

}


