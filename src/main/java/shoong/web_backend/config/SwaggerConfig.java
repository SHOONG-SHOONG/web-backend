package shoong.web_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String ACCESS_TOKEN_NAME = "Access Token";
    private static final String REFRESH_TOKEN_NAME = "Refresh Token";

    @Bean
    public OpenAPI openAPI() {
        // 보안 요구 사항
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(ACCESS_TOKEN_NAME);
        // .addList(REFRESH_TOKEN_NAME)

        // Access Token: access 헤더 사용 (Bearer 타입 아님)
        SecurityScheme accessTokenScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("access");  // 실제 JWTFilter가 읽는 헤더 이름

        // Refresh Token: refresh 헤더 사용
//        SecurityScheme refreshTokenScheme = new SecurityScheme()
//                .type(SecurityScheme.Type.APIKEY)
//                .in(SecurityScheme.In.HEADER)
//                .name("refresh");  // 실제 사용하는 헤더 이름

        return new OpenAPI()
                .addServersItem(new Server().url("/").description("https 설정"))
                .info(apiInfo())
                .addSecurityItem(securityRequirement)
                .components(new Components()
                        .addSecuritySchemes(ACCESS_TOKEN_NAME, accessTokenScheme));
        //.addSecuritySchemes(REFRESH_TOKEN_NAME, refreshTokenScheme));
    }

    private Info apiInfo() {
        return new Info()
                .title("Shoong Web API")
                .description("Shoong")
                .version("1.0.0");
    }
}
