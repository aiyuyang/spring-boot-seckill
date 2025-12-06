package com.example.seckill_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(
                new Info()
                    .title("秒杀系统 API 文档")
                    .version("v1.0")
                    .description("基于 Spring Boot + Redis + RabbitMQ 的高并发秒杀系统")

            ).addSecurityItem(
                new SecurityRequirement()
                    .addList("BearerAuth")

            ).components(
                new Components()
                    .addSecuritySchemes(
                        "BearerAuth", 
                        new SecurityScheme()
                            .name("BearerAuth")
                            .type(SecurityScheme.Type.HTTP)     // 类型是 HTTP
                            .scheme("bearer")           // 方案是 bearer
                            .bearerFormat("JWT")  // 格式是 JWT
                    )
            );
    }
}
