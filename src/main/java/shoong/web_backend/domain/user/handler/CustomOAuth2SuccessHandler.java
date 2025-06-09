package shoong.web_backend.domain.user.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import shoong.web_backend.domain.user.dto.oauth2.CustomOAuth2User;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.enums.UserRole;
import shoong.web_backend.domain.user.jwt.JWTUtil;
import shoong.web_backend.domain.user.repository.UserRepository;
import shoong.web_backend.domain.user.service.RefreshTokenService;
import shoong.web_backend.domain.user.util.CookieUtil;
import shoong.web_backend.generator.NickNameGenerator;


// OAuth2 로그인 성공 후 JWT 발급
// access, refresh -> secured 쿠키(https)
@Component // 스프링 빈으로 등록
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException, UnsupportedEncodingException {
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String name = customOAuth2User.getName(); // 실제 이름
        String username = customOAuth2User.getUsername(); // DB 저장용 식별자

        User user = userRepository.findByUserName(username);
        String userAlias = user.getUserAlias();  // DB에 저장된 별명 가져오기

        String email = customOAuth2User.getEmail();
        String role = UserRole.CLIENT.toString();
        // 사용자 존재 여부 확인 및 저장
        System.out.println("유저 고유 ID" + username);
        long id = user.getId();
        Integer expireS = 24 * 60 * 60;
        // 엑세스 토큰 유효 시간 설정 (12시간)
        long accessTokenExpiry = 12 * 60 * 60 * 1000L;
    
        String access = jwtUtil.createJwt("access", username, role, id, userAlias,
                accessTokenExpiry);
        String refresh = jwtUtil.createJwt("refresh", username, role, id, userAlias,
                expireS * 1000L);


        // refresh 토큰 DB 저장
        refreshTokenService.saveRefresh(username, expireS, refresh);

        response.addCookie(CookieUtil.createCookie("access", access, (int)(accessTokenExpiry / 1000L)));
        response.addCookie(CookieUtil.createCookie("refresh", refresh, expireS));


        String encodedName = URLEncoder.encode(name, "UTF-8");
        System.out.println("리다이렉션 정상 실행");
        response.sendRedirect("https://shoong.store/oauth2-jwt-header?name=" + encodedName);

    }
}
