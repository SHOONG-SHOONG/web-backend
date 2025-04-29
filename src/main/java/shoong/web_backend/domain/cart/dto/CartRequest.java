package shoong.web_backend.domain.cart.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartRequest {
    private Long itemId;
    private int quantity;
    Long userId = 1L; // 임시 로그인 유저
}