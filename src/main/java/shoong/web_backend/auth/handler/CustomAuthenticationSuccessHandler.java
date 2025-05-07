package shoong.web_backend.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import shoong.web_backend.domain.user.dto.UserLoginResponseDTO;

import java.io.IOException;
import java.net.URLEncoder;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String redirectUri = System.getenv("REDIRECT_URI");


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // 서버세션에 저장해놨던 access_token 가져오기 (oAuth2User에서 access token 가져오는 건 불가능)
        String accessToken = (String) request.getSession().getAttribute("access_token");
        UserLoginResponseDTO user = (UserLoginResponseDTO) request.getSession().getAttribute("user");

        if (accessToken != null && user != null) {

            String redirectUrl = String.format(
                    "http://localhost:3000/auth/callback?access_token=%s",
                    URLEncoder.encode(accessToken, "UTF-8")
            );
            response.sendRedirect(redirectUrl);
        } else {
            response.sendRedirect("http://localhost:3000/login");
        }
    }
}
