package shoong.web_backend.domain.user.service;

import java.util.Optional;
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
    public void saveRefresh(String username, Integer expireS, String newRefresh) {
        // 1. 해당 username의 기존 refresh 토큰이 있는지 조회
        Optional<Refresh> existingRefresh = refreshRepository.findByUsername(username); // findByUsername 메서드 추가 필요

        if (existingRefresh.isPresent()) {
            // 2. 존재하면 기존 엔티티 업데이트
            Refresh refreshEntity = existingRefresh.get();
            refreshEntity.setRefresh(newRefresh);
            refreshEntity.setExpiration(new Date(System.currentTimeMillis() + expireS * 1000L).toString());
            refreshRepository.save(refreshEntity); // save는 업데이트 역할을 함
        } else {
            // 3. 존재하지 않으면 새로운 엔티티 저장
            Refresh refreshEntity = Refresh.builder()
                    .username(username)
                    .refresh(newRefresh)
                    .expiration(new Date(System.currentTimeMillis() + expireS * 1000L).toString())
                    .build();
            refreshRepository.save(refreshEntity);
        }
    }
}
