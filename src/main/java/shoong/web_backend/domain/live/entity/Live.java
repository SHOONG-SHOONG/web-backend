package shoong.web_backend.domain.live.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import shoong.web_backend.domain.live.enums.LiveStatus;
import shoong.web_backend.domain.live_item.entity.LiveItem;
import shoong.web_backend.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Live {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    private String imageUrl;

    private LocalDateTime liveDate;

    @Enumerated(EnumType.STRING)
    private LiveStatus liveStatus;

    private LocalDateTime liveStartTime;
    private LocalDateTime liveEndTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "live", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LiveItem> liveItems= new ArrayList<>();
}