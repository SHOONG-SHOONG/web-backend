package shoong.web_backend.Config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().addServersItem(new Server().url("/").description("https설정"))
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("Shoong Web API")
                .description("Shoong")
                .version("1.0.0");
    }
}
