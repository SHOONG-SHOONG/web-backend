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


@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher(); // 직접 인스턴스화

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return Arrays.stream(WhiteList.WHITELIST)
                .anyMatch(whiteUri -> pathMatcher.match(whiteUri, path));
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {


        // access token 가져오기
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

        if (access == null || access.isEmpty() || access.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            System.out.println("JWTFilter - No Access Token found or it's empty/blank for authenticated path. Sending 401.");
            return;
        }

        // 토큰 만료 여부 확인
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

        // 토큰 카테고리 체크
        String category = jwtUtil.getCategory(access);
        if (!"access".equals(category)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 토큰에서 사용자 정보 추출 후 인증 설정
        long userId = jwtUtil.getUserId(access);
        String username = jwtUtil.getUsername(access);
        String role = jwtUtil.getRole(access);
        String userAlias = jwtUtil.getUserAlias(access);

        User userPrincipal = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for ID: " + userId));


        CustomUserDetails customUserDetails = new CustomUserDetails(userPrincipal);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        System.out.println("JWTFilter - Authentication for " + username + " set in SecurityContextHolder.");
        System.out.println("JWTFilter - Is authenticated: " + SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
        System.out.println("JWTFilter - Principal class: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal().getClass().getName());
        System.out.println("JWTFilter - Authorities: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());

        filterChain.doFilter(request, response);
    }
}
