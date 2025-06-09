package shoong.web_backend.domain.live.entity;

import jakarta.persistence.*;
import lombok.*;
import shoong.web_backend.domain.live.enums.LiveStatus;
import shoong.web_backend.domain.live_item.entity.LiveItem;
import shoong.web_backend.domain.user.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Live {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    private String imageUrl;
    // OBS에서 등록하는 스트림 키
    private String streamKey;

    private LocalDate liveDate;
    // 다시보기 URL
    private String replayURL;

    @Enumerated(EnumType.STRING)
    private LiveStatus liveStatus;

    private LocalDateTime liveStartTime;
    private LocalDateTime liveEndTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "live", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LiveItem> liveItems = new ArrayList<>();
}
