package shoong.web_backend.domain.user.jwt;


import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import shoong.web_backend.config.WhiteList;
import shoong.web_backend.domain.user.dto.form.CustomUserDetails;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.repository.UserRepository;

/**
    JWTFilter에서 매 요청마다 액세스 토큰 검증

    1. 요청 헤더에서 액세스 토큰 추출
    2. 토큰 존재 여부, 만료 여부, 토큰 유형 검사
    3. 검증 성공 시 인증 정보를 SecurityContextHolder에 설정
    4. 이를 통해 보호된 API 접근 가능
**/
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    // private final AntPathMatcher pathMatcher = new AntPathMatcher(); // <-- 필요 없으면 제거 가능 (shouldNotFilter에서 사용 안 함)
    private final UserRepository userRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher(); // 직접 인스턴스화

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // AntPathMatcher를 사용하여 화이트리스트 패턴을 정확히 매칭합니다.
        return Arrays.stream(WhiteList.WHITELIST)
                .anyMatch(whiteUri -> pathMatcher.match(whiteUri, path));
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // ⭐⭐⭐ 이 부분 제거 ⭐⭐⭐
        // String uri = request.getRequestURI();
        // for (String whiteUri : WhiteList.WHITELIST) {
        //     if (pathMatcher.match(whiteUri, uri)) {
        //         filterChain.doFilter(request, response);
        //         return;
        //     }
        // }
        // ⭐⭐⭐ 여기까지 제거 ⭐⭐⭐

        // 2. access token 가져오기 (헤더 우선, 없으면 쿠키)
        String access = request.getHeader("access");
        if (access == null || access.isEmpty() || access.isBlank()) { // <-- 이전에 추가한 null, empty, blank 검사 유지
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName()) || "access".equals(cookie.getName())) {
                        access = cookie.getValue();
                        break;
                    }
                }
            }
        }

        // ⭐⭐⭐ 최종적으로 토큰이 없는 경우 401 반환 로직 강화 (이전 제안 유지) ⭐⭐⭐
        if (access == null || access.isEmpty() || access.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            System.out.println("JWTFilter - No Access Token found or it's empty/blank for authenticated path. Sending 401.");
            return;
        }

        // 3. 토큰 만료 여부 확인
        try {
            jwtUtil.isExpired(access);
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            System.out.println("JWTFilter - Access Token EXPIRED! Sending 401.");
            return;
        } catch (IllegalArgumentException e) { // <-- 유효하지 않은 토큰 형식에 대한 추가 방어 코드
            System.out.println("JWTFilter - Invalid Access Token format: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 4. 토큰 카테고리 체크
        String category = jwtUtil.getCategory(access);
        if (!"access".equals(category)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 5. 토큰에서 사용자 정보 추출 후 인증 설정
        long userId = jwtUtil.getUserId(access);
        String username = jwtUtil.getUsername(access);
        String role = jwtUtil.getRole(access);
        String userAlias = jwtUtil.getUserAlias(access); // JWT에 userAlias 포함되어 있다면

        User userPrincipal = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for ID: " + userId));


        CustomUserDetails customUserDetails = new CustomUserDetails(userPrincipal);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // ⭐⭐⭐ 디버그 로그 추가 (문제 진단에 도움됨) ⭐⭐⭐
        System.out.println("JWTFilter - Authentication for " + username + " set in SecurityContextHolder.");
        System.out.println("JWTFilter - Is authenticated: " + SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
        System.out.println("JWTFilter - Principal class: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal().getClass().getName());
        System.out.println("JWTFilter - Authorities: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());


        // 6. 다음 필터 진행
        filterChain.doFilter(request, response);
    }
}
