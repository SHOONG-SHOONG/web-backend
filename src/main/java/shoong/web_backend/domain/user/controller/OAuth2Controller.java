package shoong.web_backend.domain.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import shoong.web_backend.domain.user.service.oauth2.OAuth2JwtHeaderService;

// OAuth2 로그인 -> 리다이렉트 쿠키로 토큰을 보낸다.(액세스+리프레시)
// 백엔드 서버로 재요청 시에 헤더에 담아서 보낸다.
// 프론트엔드는 localstorage 에 액세스 토큰을 저장
@RestController
@RequiredArgsConstructor
public class OAuth2Controller {
    private final OAuth2JwtHeaderService oAuth2JwtHeaderService;

    @PostMapping("/oauth2-jwt-header")
    public String oauth2JwtHeader(HttpServletRequest request, HttpServletResponse response) {
        return oAuth2JwtHeaderService.oauth2JwtHeaderSet(request, response);
    }
}