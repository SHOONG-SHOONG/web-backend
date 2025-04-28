package shoong.web_backend.domain.item.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shoong.web_backend.domain.brand.entity.Brand;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    // Item -> Brand (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    private String itemName;

    private Long price;

    private Double discountRate;

    private String description;

    private Integer itemQuantity;

    private String category;

    private LocalDateTime createdAt;

    private LocalDateTime discountExpiredAt;

    private Integer status;  // 예를 들면 0=판매중, 1=품절, 2=삭제 등
}
