package shoong.web_backend.domain.orders.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderCreateRequestDto {
    private List<Long> selectedCartIds;

    public OrderCreateRequestDto(List<Long> selectedCartIds) {
        this.selectedCartIds = selectedCartIds;
    }
}