package shoong.web_backend.domain.brand.entity;

import jakarta.persistence.*;
import lombok.*;
import shoong.web_backend.domain.item.entity.Item;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long brandId;

    private String brandName;

    private String logoUrl;

    private String brandDescription;

    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Item> items = new ArrayList<>();
}
