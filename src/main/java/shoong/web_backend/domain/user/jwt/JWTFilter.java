package shoong.web_backend.domain.user.jwt;


import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import shoong.web_backend.domain.user.dto.form.CustomUserDetails;
import shoong.web_backend.domain.user.entity.User;
import shoong.web_backend.domain.user.enums.UserRole;
/**
    JWTFilter에서 매 요청마다 액세스 토큰 검증

    1. 요청 헤더에서 액세스 토큰 추출
    2. 토큰 존재 여부, 만료 여부, 토큰 유형 검사
    3. 검증 성공 시 인증 정보를 SecurityContextHolder에 설정
    4. 이를 통해 보호된 API 접근 가능
**/
@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String access = null;
        access = request.getHeader("access");

        // 로그에 access token 출력
        if (access != null) {
            System.out.println("Access Token from header: " + access);
        }

        // 2. access 없으면 쿠키에서 accessToken 찾기
        if (access == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("accessToken") || cookie.getName().equals("access")) {
                        access = cookie.getValue();
                        break;
                    }
                }
            }
        }

        // 로그에 access token 출력 (쿠키에서 찾은 경우)
        if (access != null) {
            System.out.println("Access Token from cookie: " + access);
        }

        // access token null
        if (access == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // access token expired
        try {
            jwtUtil.isExpired(access);
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String category = jwtUtil.getCategory(access);

        // not access token
        if (!category.equals("access")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        long userId = jwtUtil.getUserId(access);
        String username = jwtUtil.getUsername(access);
        String role = jwtUtil.getRole(access);

        User userPrincipal = User.builder()
                .id(userId)
                .userName(username)
                .role(UserRole.valueOf(role))
                .userPassword("temp_pw")
                .build();

        CustomUserDetails customUserDetails = new CustomUserDetails(userPrincipal);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}