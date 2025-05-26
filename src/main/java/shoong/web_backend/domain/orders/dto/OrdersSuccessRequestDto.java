package shoong.web_backend.domain.orders.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class OrdersSuccessRequestDto {
    private Long orderId;
    private String orderAddress;
}