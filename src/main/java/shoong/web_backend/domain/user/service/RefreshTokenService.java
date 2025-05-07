package shoong.web_backend.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import shoong.web_backend.domain.user.entity.Refresh;
import shoong.web_backend.domain.user.repository.RefreshRepository;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshRepository refreshRepository;
    @Transactional
    public void saveRefresh(String username, Integer expireS, String refresh) {
        Refresh refreshEntity = Refresh.builder()
                .username(username)
                .refresh(refresh)
                .expiration(new Date(System.currentTimeMillis() + expireS * 1000L).toString())
                .build();
        refreshRepository.save(refreshEntity);
    }
}
