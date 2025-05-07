package shoong.web_backend.domain.cart.dto;

import lombok.Getter;
import lombok.Setter;
import shoong.web_backend.domain.cart.entity.Cart;

@Getter
@Setter
public class CartResponseDto {
    private Long cartId;
    private Long itemId;
    private int cartQuantity;
    private String itemName;
    private Long price;
    private Double discountRate;
    private String category;
    private String description;

    //장바구니 조회
    public static CartResponseDto from(Cart cart) {
        CartResponseDto response = new CartResponseDto();
        response.setCartId(cart.getCartId());
        response.setItemId(cart.getItem().getItemId());
        response.setCartQuantity(cart.getCartQuantity());
        response.setItemName(cart.getItem().getItemName());
        response.setPrice(cart.getItem().getPrice());
        response.setDiscountRate(cart.getItem().getDiscountRate());
        response.setCategory(cart.getItem().getCategory());
        response.setDescription(cart.getItem().getDescription());
        return response;
    }
}

