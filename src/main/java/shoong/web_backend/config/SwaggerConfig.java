package shoong.web_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;

// OpenAPIDefinition과 Info를 클래스 레벨에서 정의
@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(title = "Shoong Web API", description = "Shoong", version = "1.0.0")
)
@Configuration
public class SwaggerConfig {

    private static final String ACCESS_TOKEN_SCHEME_NAME = "AccessTokenHeader"; // Swagger UI에서 보여줄 보안 스키마 이름

    @Bean
    public OpenAPI openAPI() {
        // 보안 요구 사항: "AccessTokenHeader"라는 이름으로 정의된 보안 스키마를 사용
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(ACCESS_TOKEN_SCHEME_NAME);

        // Access Token: access 헤더 사용을 명시 (APIKEY 타입으로)
        SecurityScheme accessTokenScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY) // API 키 방식으로 정의
                .in(SecurityScheme.In.HEADER)    // 헤더에 토큰을 포함
                .name("access")                  // 실제 헤더 이름은 "access"
                .description("액세스 토큰을 'access' 헤더에 직접 입력하세요. (예: abc.xyz.123)"); // 설명 추가

        return new OpenAPI()
                .addServersItem(new Server().url("/").description("현재 서버 URL")) // 서버 URL 설정
                .info(apiInfo()) // API 정보 설정 (클래스 레벨 @OpenAPIDefinition에서 처리해도 됨)
                .addSecurityItem(securityRequirement) // 이 API에 보안 요구사항 적용
                .components(new Components()
                        .addSecuritySchemes(ACCESS_TOKEN_SCHEME_NAME, accessTokenScheme)); // 보안 스키마 컴포넌트 추가
    }

    private Info apiInfo() {
        return new Info()
                .title("Shoong Web API")
                .description("Shoong")
                .version("1.0.0");
    }
}