package cl.duoc.aromaesencial.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentación Swagger: http://localhost:8080/swagger-ui.html
 * Incluye el botón "Authorize" para probar endpoints protegidos con el token JWT (Parte 4).
 *
 * Cada controlador debe usar &#64;Tag(name = "...") y cada método &#64;Operation(summary = "...")
 * para que los endpoints queden bien documentados (criterio IE3.2.1).
 */
@Configuration
public class OpenApiConfig {

    public static final String ESQUEMA_JWT = "bearerAuth";

    @Bean
    public OpenAPI aromaEsencialOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AromaEsencial API")
                        .version("v1")
                        .description("API REST de la tienda de perfumes AromaEsencial: catálogo, boletas, "
                                + "usuarios y autenticación con JWT. Roles: ADMINISTRADOR, VENDEDOR y CLIENTE."))
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT,
                        new SecurityScheme()
                                .name(ESQUEMA_JWT)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
    }
}
