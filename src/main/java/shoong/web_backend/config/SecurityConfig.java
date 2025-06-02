package shoong.web_backend.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.SerializationUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.WebUtils;
import shoong.web_backend.domain.user.handler.CustomFormSuccessHandler;
import shoong.web_backend.domain.user.handler.CustomLogoutFilter;
import shoong.web_backend.domain.user.handler.CustomLogoutSuccessHandler;
import shoong.web_backend.domain.user.handler.CustomOAuth2SuccessHandler;
import shoong.web_backend.domain.user.jwt.JWTFilter;
import shoong.web_backend.domain.user.jwt.JWTUtil;
import shoong.web_backend.domain.user.repository.RefreshRepository;
import shoong.web_backend.domain.user.repository.UserRepository;
import shoong.web_backend.domain.user.service.RefreshTokenService;
import shoong.web_backend.domain.user.service.oauth2.CustomOAuth2UserService;
@EnableWebSecurity
@EnableMethodSecurity  // ✅ 이거 추가!
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JWTUtil jwtUtil;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshRepository refreshRepository;
    private final UserRepository userRepository;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomFormSuccessHandler customFormSuccessHandler; // 스프링 컨테이너에서 주입
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler; // 스프링 컨테이너에서 주입
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler; // 새로 생성한 핸들러 주입

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            System.out.println("Authentication Failed: " + exception.getMessage());
            response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 401 Unauthorized
        };
    }

    // CORS 설정을 별도의 Bean 메서드로 분리하여 가독성과 재사용성을 높임
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 오리진 패턴 설정 (와일드카드 사용 가능)
        configuration.setAllowedOriginPatterns(
                List.of("https://shoong.store",
                        "https://*.shoong.store", // 서브도메인 포함
                        "http://192.168.0.6",
                        "http://192.168.0.5",// 내부 네트워크 IP (개발용)
                        "http://localhost:3000") // 로컬 개발용
        );
        configuration.setAllowedMethods(Collections.singletonList("*")); // 모든 HTTP 메서드 허용
        configuration.setAllowCredentials(true); // 자격 증명 (쿠키, HTTP 인증 등) 허용
        configuration.setAllowedHeaders(Collections.singletonList("*")); // 모든 헤더 허용
        configuration.setMaxAge(3600L); // Pre-flight 요청 캐싱 시간 (초)
        configuration.setExposedHeaders(Collections.singletonList("access")); // 클라이언트가 접근할 수 있는 응답 헤더

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 CORS 설정 적용
        return source;
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic((basic) -> basic.disable()) // HTTP Basic 인증 비활성화
                .csrf((csrf) -> csrf.disable()) // CSRF 보호 비활성화 (JWT/Stateless API에 적합)
                .formLogin((form) -> form
                        .loginPage("/login") // 커스텀 로그인 페이지 URL
                        .loginProcessingUrl("/login") // 폼 로그인 처리 URL
                        .successHandler(customFormSuccessHandler) // 주입된 성공 핸들러 사용
                        .failureHandler(authenticationFailureHandler()) // 실패 핸들러 사용
                        .permitAll()) // 로그인 페이지는 모든 사용자 접근 허용
                .oauth2Login((oauth2) -> oauth2
                        .loginPage("/login") // OAuth2 로그인 페이지 URL
                        .userInfoEndpoint((userinfo) -> userinfo.userService(customOAuth2UserService)) // 사용자 정보 서비스 설정
                        .successHandler(customOAuth2SuccessHandler) // 주입된 OAuth2 성공 핸들러 사용
                        .failureHandler(authenticationFailureHandler()) // 실패 핸들러 사용
                        .permitAll()) // OAuth2 로그인 페이지는 모든 사용자 접근 허용
                .logout((logout) -> logout
                        .logoutUrl("/logout") // 로그아웃 요청을 처리할 URL
                        .logoutSuccessHandler(customLogoutSuccessHandler) // 커스텀 로그아웃 성공 핸들러 사용
                        .deleteCookies("refresh") // 로그아웃 시 "refresh" 쿠키 삭제
                        .permitAll()) // 로그아웃 URL은 모든 사용자 접근 허용
                .cors((cors) -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 적용
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(WhiteList.WHITELIST).permitAll() // 화이트리스트 URL은 인증 없이 접근 허용
                        .requestMatchers("/admin").hasRole("ADMIN") // "/admin" 경로는 ADMIN 역할만 접근 가능
                        .anyRequest().authenticated()) // 그 외 모든 요청은 인증 필요
                .exceptionHandling(
                        (exception) -> exception
                                .authenticationEntryPoint(customAuthenticationEntryPoint)) // 인증되지 않은 요청 처리
                .addFilterBefore(new JWTFilter(jwtUtil,userRepository), UsernamePasswordAuthenticationFilter.class) // JWT 검증 필터 추가
                // 기존 CustomLogoutFilter는 제거 (CustomLogoutSuccessHandler가 대체)
                // .addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshRepository), LogoutFilter.class)
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // 세션 사용 안 함 (JWT 기반 인증)
        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/actuator/**") // actuator 관련 요청만 처리
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .httpBasic(Customizer.withDefaults()); // Prometheus는 기본 인증 지원 가능성 있음

        return http.build();
    }
}


