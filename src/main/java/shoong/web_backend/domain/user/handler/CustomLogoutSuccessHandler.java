package shoong.web_backend.domain.user.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional; // Optional 임포트 추가
import shoong.web_backend.domain.user.jwt.JWTUtil;
import shoong.web_backend.domain.user.repository.RefreshRepository;

@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 1. 요청 쿠키에서 "refresh" 토큰 가져오기
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();

        if (cookies != null) { // 쿠키가 null이 아닌지 먼저 체크
            Optional<String> refreshOpt = Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals("refresh"))
                    .map(Cookie::getValue)
                    .findFirst(); 

            if (refreshOpt.isPresent()) {
                refreshToken = refreshOpt.get();
            }
        }

        // 2. 리프레시 토큰이 없으면 400 Bad Request 반환 후 로직 종료
        if (refreshToken == null) {
            System.out.println("리프레시 토큰 없음: 로그아웃 처리 불필요 또는 잘못된 요청");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
            return;
        }

        // 3. 리프레시 토큰의 유효성 및 카테고리 확인
        String category = jwtUtil.getCategory(refreshToken); 

        if (!"refresh".equals(category)) { // 리프레시 토큰이 아닌 경우
            System.out.println("유효하지 않은 토큰 카테고리: " + category);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
            return;
        }

        // 4. DB에 리프레시 토큰 존재 여부 확인 및 삭제
        Boolean isExist = refreshRepository.existsByRefresh(refreshToken);

        if (isExist) { // DB에 존재하는 경우만 삭제
            refreshRepository.deleteByRefresh(refreshToken);
            System.out.println("DB에서 리프레시 토큰 삭제 완료.");
        } else {
            System.out.println("DB에 리프레시 토큰이 존재하지 않음 (이미 삭제되었거나 유효하지 않음).");
        }

        // 5. 로그아웃 성공 응답 전송
        response.setStatus(HttpServletResponse.SC_OK); // 200 OK
        System.out.println("로그아웃 성공 처리 완료.");

    }
}