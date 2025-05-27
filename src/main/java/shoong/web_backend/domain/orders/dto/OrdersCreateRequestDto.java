package shoong.web_backend.domain.orders.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrdersCreateRequestDto {
    private List<Long> selectedCartIds;

    public OrdersCreateRequestDto(List<Long> selectedCartIds) {
        this.selectedCartIds = selectedCartIds;
    }
}