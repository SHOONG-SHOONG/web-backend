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
import shoong.web_backend.domain.user.dto.oauth2.CustomOAuth2User;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.enums.UserRole;
import shoong.web_backend.domain.user.jwt.JWTUtil;
import shoong.web_backend.domain.user.repository.UserRepository;
import shoong.web_backend.domain.user.service.RefreshTokenService;
import shoong.web_backend.domain.user.util.CookieUtil;

/**
 * OAuth2 로그인 성공 후 JWT 발급
 * access, refresh -> httpOnly 쿠키
 * 리다이렉트 되기 때문에 헤더로 전달 불가능
 */
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException, UnsupportedEncodingException {
        // create JWT
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String name = customOAuth2User.getName(); // 실제 이름
        String username = customOAuth2User.getUsername(); // DB 저장용 식별자
        String email = customOAuth2User.getEmail();
        // 카카오 소셜 로그인 유저는 애초에
        String role = UserRole.CLIENT.toString();
        //String role = authentication.getAuthorities().iterator().next().getAuthority();
        // 사용자 존재 여부 확인 및 저장
        System.out.println("유저 고유 ID" + username);
        User user = userRepository.findByUserName(username);
        long id = user.getId();

        Integer expireS = 24 * 60 * 60;
        String access = jwtUtil.createJwt("access", username, role, id,60 * 10 * 1000L);
        String refresh = jwtUtil.createJwt("refresh", username, role, id,expireS * 1000L);

        System.out.println("엑세스 토큰" + access);
        System.out.println("리프레시 토큰" + refresh);
        // refresh 토큰 DB 저장
        refreshTokenService.saveRefresh(username, expireS, refresh);

        response.addCookie(CookieUtil.createCookie("access", access, 60 * 10));
        response.addCookie(CookieUtil.createCookie("refresh", refresh, expireS));

        // redirect query param 인코딩 후 전달
        // 이후에 JWT 를 읽어서 데이터를 가져올 수도 있지만, JWT 파싱 비용이 많이 들기 때문에
        // 처음 JWT 발급할 때 이름을 함께 넘긴 후, 로컬 스토리지에 저장한다.
        String encodedName = URLEncoder.encode(name, "UTF-8");
        response.sendRedirect("http://localhost:3000/oauth2-jwt-header?name=" + encodedName);
    }

}