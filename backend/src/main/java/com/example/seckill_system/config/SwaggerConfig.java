package com.example.seckill_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Swagger/OpenAPI Configuration - API Documentation
 * 
 * <p>This configuration class sets up Swagger UI for interactive API documentation.
 * Swagger provides a web-based interface where developers can explore all API endpoints,
 * view request/response schemas, and test API calls directly from the browser.</p>
 * 
 * <p><b>Access</b>:</p>
 * <p>After starting the application, Swagger UI is available at:</p>
 * <pre>{@code
 * http://localhost:8081/swagger-ui.html
 * }</pre>
 * 
 * <p><b>Features</b>:</p>
 * <ul>
 *   <li><b>Interactive API Explorer</b>: Test endpoints directly from the browser</li>
 *   <li><b>Request/Response Schemas</b>: View data structures for all endpoints</li>
 *   <li><b>Authentication Support</b>: Configure JWT token for authenticated endpoints</li>
 *   <li><b>Auto-Generated Documentation</b>: Documentation is generated from code annotations</li>
 * </ul>
 * 
 * <p><b>Security Configuration</b>:</p>
 * <p>The configuration includes JWT Bearer token authentication support. Users can
 * enter their JWT token in Swagger UI, and it will be included in all API requests
 * via the Authorization header.</p>
 * 
 * <p><b>Production Considerations</b>:</p>
 * <p>In production, Swagger should typically be disabled or restricted to internal
 * networks only, as it exposes API structure and could be a security risk.</p>
 * 
 * @author Ai Yuyang
 * @see <a href="https://swagger.io/">Swagger/OpenAPI Documentation</a>
 */
@Configuration
public class SwaggerConfig {
    
    /**
     * Configures the OpenAPI specification for Swagger UI.
     * 
     * <p>This bean defines the API documentation metadata and security scheme.
     * The OpenAPI specification is used by Swagger UI to generate the interactive
     * API documentation interface.</p>
     * 
     * <p><b>Configuration Includes</b>:</p>
     * <ul>
     *   <li><b>API Information</b>: Title, version, and description</li>
     *   <li><b>Security Scheme</b>: JWT Bearer token authentication</li>
     *   <li><b>Global Security</b>: All endpoints require Bearer token by default</li>
     * </ul>
     * 
     * <p><b>Security Scheme</b>:</p>
     * <p>The configuration defines a "BearerAuth" security scheme that uses HTTP Bearer
     * token authentication. This allows Swagger UI to include the JWT token in the
     * Authorization header for all API requests.</p>
     * 
     * @return OpenAPI specification bean for Swagger UI
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(
                new Info()
                    .title("Seckill System API Documentation")
                    .version("v1.0")
                    .description("High-concurrency seckill system based on Spring Boot + Redis + RabbitMQ")

            ).addSecurityItem(
                // Apply Bearer token authentication to all endpoints by default
                new SecurityRequirement()
                    .addList("BearerAuth")

            ).components(
                new Components()
                    .addSecuritySchemes(
                        "BearerAuth", 
                        new SecurityScheme()
                            .name("BearerAuth")
                            .type(SecurityScheme.Type.HTTP)     // Type is HTTP
                            .scheme("bearer")           // Scheme is bearer (Authorization: Bearer <token>)
                            .bearerFormat("JWT")  // Format is JWT (JSON Web Token)
                    )
            );
    }
}
