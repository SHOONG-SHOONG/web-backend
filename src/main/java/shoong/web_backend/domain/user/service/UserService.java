package shoong.web_backend.domain.user.service;

import org.springframework.stereotype.Service;
import shoong.web_backend.domain.user.dto.UserLoginResponseDTO;

@Service
public interface UserService {
    boolean existsByKakaoId(Long kakaoId);
    UserLoginResponseDTO findByKakaoId(Long kakaoId);
    UserLoginResponseDTO save(UserLoginResponseDTO user);
}
