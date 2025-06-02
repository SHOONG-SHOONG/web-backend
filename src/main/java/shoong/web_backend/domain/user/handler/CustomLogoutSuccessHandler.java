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

@Component // 스프링 빈으로 등록
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final JWTUtil jwtUtil; // JWT 관련 유틸리티 (토큰 파싱 등)
    private final RefreshRepository refreshRepository; // 리프레시 토큰 저장소

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 1. 요청 쿠키에서 "refresh" 토큰 값 가져오기
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();

        if (cookies != null) { // 쿠키가 null이 아닌지 먼저 체크
            Optional<String> refreshOpt = Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals("refresh"))
                    .map(Cookie::getValue)
                    .findFirst(); // Optional로 처리

            if (refreshOpt.isPresent()) {
                refreshToken = refreshOpt.get();
            }
        }

        // 2. 리프레시 토큰이 없으면 (예: 이미 만료되었거나 없음) 400 Bad Request 반환 후 종료
        if (refreshToken == null) {
            System.out.println("리프레시 토큰 없음: 로그아웃 처리 불필요 또는 잘못된 요청");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
            return;
        }

        // 3. 리프레시 토큰의 유효성 및 카테고리 (refresh) 확인
        // (참고: JWTUtil.getCategory() 내부에서 토큰 파싱 오류 시 예외 발생할 수 있음)
        String category = jwtUtil.getCategory(refreshToken); // JWTUtil에서 토큰 검증 로직이 있다고 가정

        if (!"refresh".equals(category)) { // "refresh" 토큰이 아닌 경우
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
            // 이미 삭제된 토큰이거나 유효하지 않은 경우이므로 굳이 400을 반환할 필요는 없습니다.
            // 클라이언트에게는 성공적인 로그아웃으로 보여줄 수 있습니다.
        }

        // 5. 클라이언트 쿠키에서 "refresh" 토큰 삭제
        // Spring Security의 .deleteCookies("refresh")가 이 역할을 수행하므로,
        // 여기서는 명시적으로 쿠키를 추가하지 않아도 됩니다.
        // 만약 Spring Security의 deleteCookies 기능을 사용하지 않는다면 아래 코드 사용
        /*
        Cookie deleteCookie = new Cookie("refresh", null);
        deleteCookie.setMaxAge(0); // 쿠키 즉시 만료
        deleteCookie.setPath("/"); // 모든 경로에 적용
        response.addCookie(deleteCookie);
        */

        // 6. 로그아웃 성공 응답 전송
        response.setStatus(HttpServletResponse.SC_OK); // 200 OK
        System.out.println("로그아웃 성공 처리 완료.");

        // 필요하다면 로그아웃 후 리다이렉션
        // response.sendRedirect("/logout-success-page");
    }
}