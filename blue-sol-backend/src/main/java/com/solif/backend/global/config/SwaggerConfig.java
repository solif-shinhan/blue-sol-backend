package com.solif.backend.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("blue-sol API Document")
                .version("v1.0.0")
                .description("solif 프로젝트의 API 명세서입니다.");

        return new OpenAPI()
                .addServersItem(new Server().url("/"))
                .info(info);
    }
}
