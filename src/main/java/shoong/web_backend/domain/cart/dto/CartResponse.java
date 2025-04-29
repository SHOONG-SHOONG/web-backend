package shoong.web_backend.domain.cart.dto;

import lombok.Getter;
import lombok.Setter;
import shoong.web_backend.domain.cart.entity.Cart;

@Getter
@Setter
public class CartResponse {
    private Long cartId;
    private Long itemId;
    private int quantity;
    private String itemName;
    private Long price;
    private Double discountRate;
    private String category;
    private String description;

    public static CartResponse from(Cart cart) {
        CartResponse response = new CartResponse();
        response.setCartId(cart.getCartId());
        response.setItemId(cart.getItem().getItemId());
        response.setQuantity(cart.getQuantity());
        response.setItemName(cart.getItem().getItemName());
        response.setPrice(cart.getItem().getPrice());
        response.setDiscountRate(cart.getItem().getDiscountRate());
        response.setCategory(cart.getItem().getCategory());
        response.setDescription(cart.getItem().getDescription());
        return response;
    }
}

