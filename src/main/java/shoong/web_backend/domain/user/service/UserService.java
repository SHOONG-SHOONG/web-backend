package shoong.web_backend.domain.user.service;

import org.springframework.stereotype.Service;
import shoong.web_backend.domain.user.dto.UserDTO;

@Service
public interface UserService {
    boolean existsByKakaoId(Long kakaoId);
    UserDTO findByKakaoId(Long kakaoId);
    UserDTO save(UserDTO user);
}
