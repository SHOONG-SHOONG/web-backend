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

        // NullPointerException 방지
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

        // 만료된 토큰은 payload 읽을 수 없음 -> ExpiredJwtException 발생
        try {
            jwtUtil.isExpired(refresh);
        } catch(ExpiredJwtException e){
            return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
        }

        // refresh 토큰이 아님
        String category = jwtUtil.getCategory(refresh);
        if(!"refresh".equals(category)) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);
        String userAlias = jwtUtil.getUserAlias(refresh);
        long userId = jwtUtil.getUserId(refresh);

        // refresh DB 조회 - 이 부분은 이제 refresh 토큰 자체의 유효성 검증용
        // 현재 코드에서는 유효한 refresh 토큰을 사용하는지 확인하는 용도로 적합
        Boolean isExist = refreshRepository.existsByRefresh(refresh);

        // DB 에 없는 리프레시 토큰 (혹은 블랙리스트 처리된 리프레시 토큰)
        if(!isExist) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        // new tokens
        String newAccess = jwtUtil.createJwt("access", username, role, userId, userAlias,60 * 10 * 1000L);
        Integer expiredS = 60 * 60 * 24;
        String newRefresh = jwtUtil.createJwt("refresh", username, role, userId, userAlias,expiredS * 1000L);

        // **여기서 기존 refresh 토큰을 삭제하고 새로운 refresh 토큰으로 업데이트 합니다.**
        // 기존 refresh DB 삭제 (재사용 공격 방지)
        refreshRepository.deleteByRefresh(refresh); // 이전에 사용된 토큰은 즉시 무효화

        // 새로운 refresh 토큰 저장/업데이트
        // saveRefresh 내부에서 username 기준으로 기존 토큰을 찾아 업데이트하도록 구현했으므로,
        // 이제 새로운 레코드가 계속 쌓이지 않고 기존 레코드가 업데이트 됩니다.
        refreshTokenService.saveRefresh(username, expiredS, newRefresh);

        response.setHeader("access", newAccess);
        response.addCookie(CookieUtil.createCookie("refresh", newRefresh, expiredS));

        return new ResponseEntity<>(HttpStatus.OK);
    }
}