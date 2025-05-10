package shoong.web_backend.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Base64;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.util.SerializationUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.util.WebUtils;
import shoong.web_backend.domain.user.handler.CustomFormSuccessHandler;
import shoong.web_backend.domain.user.handler.CustomLogoutFilter;
import shoong.web_backend.domain.user.handler.CustomOAuth2SuccessHandler;
import shoong.web_backend.domain.user.jwt.JWTFilter;
import shoong.web_backend.domain.user.jwt.JWTUtil;
import shoong.web_backend.domain.user.repository.RefreshRepository;
import shoong.web_backend.domain.user.repository.UserRepository;
import shoong.web_backend.domain.user.service.RefreshTokenService;
import shoong.web_backend.domain.user.service.oauth2.CustomOAuth2UserService;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JWTUtil jwtUtil;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshRepository refreshRepository;
    private final UserRepository userRepository;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            System.out.println("exception = " + exception);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        };
    }

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic((basic) -> basic.disable())
                .csrf((csrf) -> csrf.disable())
                .formLogin((form) -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(new CustomFormSuccessHandler(jwtUtil, refreshTokenService))
                        .failureHandler(authenticationFailureHandler())
                        .permitAll())
                .oauth2Login((oauth2) -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint((userinfo) -> userinfo.userService(customOAuth2UserService))
                        .successHandler(new CustomOAuth2SuccessHandler(jwtUtil, refreshTokenService, userRepository))
                        .failureHandler(authenticationFailureHandler())
                        // OAuth2 인증 요청을 쿠키에 저장하도록 설정
                        .authorizationEndpoint(endpoint ->
                                endpoint.authorizationRequestRepository(authorizationRequestRepository()))
                        .permitAll())
                .logout((logout) -> logout.logoutSuccessUrl("/").permitAll())
                .cors((cors) -> cors.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration configuration = new CorsConfiguration();
                        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                        configuration.setAllowedMethods(Collections.singletonList("*"));
                        configuration.setAllowCredentials(true);
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setMaxAge(3600L);
                        configuration.setExposedHeaders(Collections.singletonList("access"));
                        return configuration;
                    }
                }))
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(
                                "/login", "/join", "/logout", "/oauth2-jwt-header",
                                "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**"
                        ).permitAll()
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling((exception) -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        }))
                .addFilterAfter(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshRepository), LogoutFilter.class)
                // 중복 제거하고 한 번만 설정
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    // 세션 대신 쿠키를 사용하여 OAuth2 인증 요청 저장
    public static class HttpCookieOAuth2AuthorizationRequestRepository
            implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

        private static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
        private static final int COOKIE_EXPIRE_SECONDS = 180;

        @Override
        public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
            Cookie cookie = WebUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            return cookie != null ? deserialize(cookie) : null;
        }

        @Override
        public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                             HttpServletRequest request, HttpServletResponse response) {
            if (authorizationRequest == null) {
                removeAuthorizationRequestCookie(request, response);
                return;
            }

            Cookie cookie = new Cookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, serialize(authorizationRequest));
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(COOKIE_EXPIRE_SECONDS);
            response.addCookie(cookie);
        }

        @Override
        public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
            OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);
            if (authRequest != null) {
                removeAuthorizationRequestCookie(request, response);
            }
            return authRequest;
        }

        private void removeAuthorizationRequestCookie(HttpServletRequest request, HttpServletResponse response) {
            Cookie cookie = new Cookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, "");
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }

        private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
            try {
                return Base64.getEncoder().encodeToString(
                        SerializationUtils.serialize(authorizationRequest));
            } catch (Exception e) {
                return "";
            }
        }

        private OAuth2AuthorizationRequest deserialize(Cookie cookie) {
            try {
                return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(
                        Base64.getDecoder().decode(cookie.getValue()));
            } catch (Exception e) {
                return null;
            }
        }
    }
}