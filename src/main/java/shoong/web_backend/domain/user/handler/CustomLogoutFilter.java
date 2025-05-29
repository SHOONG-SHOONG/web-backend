package shoong.web_backend.domain.user.handler;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;
import shoong.web_backend.domain.user.jwt.JWTUtil;
import shoong.web_backend.domain.user.repository.RefreshRepository;
import shoong.web_backend.domain.user.repository.UserRepository;
import shoong.web_backend.domain.user.util.CookieUtil;

/**
 * 로그아웃 필터
 * refresh 토큰 만료
 */
@RequiredArgsConstructor
public class CustomLogoutFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        // 로그아웃 요청 경로인지 확인
        if (!requestUri.matches("^\\/logout$")) { // "/logout" 경로에 대해서만 동작하도록
            filterChain.doFilter(request, response);
            return;
        }

        String requestMethod = request.getMethod();
        // POST 요청만 처리
        if (!requestMethod.equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 여기서부터 Refresh 토큰 처리
        String refresh = null;
        Cookie[] cookies = request.getCookies();

        // --- 이 부분이 핵심 수정 사항 ---
        // cookies 배열이 null이 아닌지 확인
        if (cookies != null) {
            Optional<Cookie> refreshCookie = Arrays.stream(cookies)
                    .filter((cookie) -> "refresh".equals(cookie.getName()))
                    .findFirst();
            if (refreshCookie.isPresent()) {
                refresh = refreshCookie.get().getValue();
            }
        }
        // --- 여기까지 ---

        // refresh 토큰이 없는 경우 (쿠키에 없거나, null 체크 후에도 null인 경우)
        if (refresh == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
            response.getWriter().write("refresh token is null"); // 적절한 에러 메시지 반환
            return;
        }

        // JWT 토큰 유효성 검사 (만료, 카테고리, DB 존재 여부)
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
            response.getWriter().write("refresh token expired");
            return;
        }

        String category = jwtUtil.getCategory(refresh);
        if (!"refresh".equals(category)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
            response.getWriter().write("invalid refresh token");
            return;
        }

        Boolean isExist = refreshRepository.existsByRefresh(refresh);
        if (!isExist) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
            response.getWriter().write("invalid refresh token (not found in DB)");
            return;
        }

        // Refresh 토큰 삭제
        refreshRepository.deleteByRefresh(refresh);

        response.setStatus(HttpServletResponse.SC_OK); // 200 OK
        response.getWriter().write("logout success"); // 성공 메시지
    }
}