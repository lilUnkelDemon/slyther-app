// src/main/java/ir/momeni/slyther/config/OpenApiConfig.java
package ir.momeni.slyther.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {

    @Bean
    public OpenAPI baseOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Slyther API")
                .version("v1")
                .description("Authentication & Authorization with JWT"));
    }

    @Bean
    public GroupedOpenApi coreGroup() {
        return GroupedOpenApi.builder()
                .group("core")
                .packagesToScan(
                        "ir.momeni.slyther.auth.controller",
                        "ir.momeni.slyther.user.controller",
                        "ir.momeni.slyther.test.controller")
                .pathsToMatch("/api/**")
                .build();
    }
}
