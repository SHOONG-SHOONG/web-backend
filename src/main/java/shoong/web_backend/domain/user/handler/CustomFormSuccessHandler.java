package shoong.web_backend.domain.user.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import shoong.web_backend.domain.user.dto.form.CustomUserDetails;
import shoong.web_backend.domain.user.jwt.JWTUtil;
import shoong.web_backend.domain.user.service.RefreshTokenService;
import shoong.web_backend.domain.user.util.CookieUtil;

/**
 * 폼 로그인 성공 후 JWT 발급
 * access -> 헤더
 * refresh -> 쿠키
 */
@RequiredArgsConstructor
public class CustomFormSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException, IOException {
        // create JWT
        // CustomUserDetails 객체 꺼내기
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        long userId = customUserDetails.getUserId();  // 👈 여기서 가져옴
        String username = authentication.getName();
        String userAlias = customUserDetails.getUserAlias();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        long expireMs = 1000L * 60 * 60 * 12; // 12시간

        // access
        String access = jwtUtil.createJwt("access", username, role ,userId, userAlias,expireMs);
        response.setHeader("access", access);

        // refresh
        Integer expireS = 24 * 60 * 60;
        String refresh = jwtUtil.createJwt("refresh", username, role, userId, userAlias,expireS * 1000L);
        response.addCookie(CookieUtil.createCookie("refresh", refresh, expireS));
        System.out.println("엑세스 토큰" + access);
        System.out.println("리프레시 토큰" + refresh);

        // refresh 토큰 DB 저장
        refreshTokenService.saveRefresh(username, expireS, refresh);

        // json 을 ObjectMapper 로 직렬화하여 전달
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("name", username);
        // 2025-05-19 JWT PayLoad에 유저 별명(alias) 추가
        // responseData.put("alias", userAlias);
        new ObjectMapper().writeValue(response.getWriter(), responseData);
    }
}
