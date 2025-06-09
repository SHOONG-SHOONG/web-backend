package shoong.web_backend.domain.user.service;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import shoong.web_backend.domain.user.jwt.JWTUtil;
import shoong.web_backend.domain.user.repository.RefreshRepository;
import shoong.web_backend.domain.user.util.CookieUtil;

@Service
@RequiredArgsConstructor
public class ReissueService {
    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;
    private final RefreshTokenService refreshTokenService;

    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refresh = null;
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            Optional<Cookie> refreshCookie = Arrays.stream(cookies)
                    .filter((cookie) -> "refresh".equals(cookie.getName()))
                    .findFirst();
            if (refreshCookie.isPresent()) {
                refresh = refreshCookie.get().getValue();
            }
        }

        // 쿠키에 refresh 토큰 x
        if (refresh == null) {
            return new ResponseEntity<>("refresh token is null", HttpStatus.BAD_REQUEST);
        }

        try {
            jwtUtil.isExpired(refresh);
        } catch(ExpiredJwtException e){
            return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
        }

        String category = jwtUtil.getCategory(refresh);
        if(!"refresh".equals(category)) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);
        String userAlias = jwtUtil.getUserAlias(refresh);
        long userId = jwtUtil.getUserId(refresh);

        Boolean isExist = refreshRepository.existsByRefresh(refresh);

        // DB 에 없는 리프레시 토큰
        if(!isExist) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        String newAccess = jwtUtil.createJwt("access", username, role, userId, userAlias,60 * 10 * 1000L);
        Integer expiredS = 60 * 60 * 24;
        String newRefresh = jwtUtil.createJwt("refresh", username, role, userId, userAlias,expiredS * 1000L);

        // 기존 refresh DB 삭제
        refreshRepository.deleteByRefresh(refresh); // 이전에 사용된 토큰은 즉시 무효화

        // 새로운 refresh 토큰 저장/업데이트
        refreshTokenService.saveRefresh(username, expiredS, newRefresh);

        response.setHeader("access", newAccess);
        response.addCookie(CookieUtil.createCookie("refresh", newRefresh, expiredS));

        return new ResponseEntity<>(HttpStatus.OK);
    }
}