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
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import shoong.web_backend.config.WhiteList;
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
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // 1. 화이트리스트 경로면 바로 통과
        for (String whiteUri : WhiteList.WHITELIST) {
            if (pathMatcher.match(whiteUri, uri)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 2. access token 가져오기 (헤더 우선, 없으면 쿠키)
        String access = request.getHeader("access");
        if (access == null) {
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

        if (access == null) {
            // 토큰 없으면 인증 실패 처리 (401) 또는 그냥 다음 필터 넘김 (원하는 정책에 맞게)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 3. 토큰 만료 여부 확인
        try {
            jwtUtil.isExpired(access);
        } catch (ExpiredJwtException e) {
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

        User userPrincipal = User.builder()
                .id(userId)
                .userName(username)
                .role(UserRole.valueOf(role))
                .userPassword("temp_pw")
                .build();

        CustomUserDetails customUserDetails = new CustomUserDetails(userPrincipal);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 6. 다음 필터 진행
        filterChain.doFilter(request, response);
    }
}
