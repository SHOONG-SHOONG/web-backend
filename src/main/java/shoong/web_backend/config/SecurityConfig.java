package shoong.web_backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
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
                .successHandler(new CustomOAuth2SuccessHandler(jwtUtil,refreshTokenService,userRepository))
                .failureHandler(authenticationFailureHandler())
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
            .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}

