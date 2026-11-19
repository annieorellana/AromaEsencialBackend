
package cl.duoc.aromaesencial.config;

import cl.duoc.aromaesencial.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ============================================================
 * CONFIGURACION DE SEGURIDAD - AROMAESENCIAL
 * INTEGRANTE B: AUTENTICACION Y AUTORIZACION
 * ============================================================
 *
 * Seguridad implementada mediante Spring Security y JWT.
 *
 * - Login, registro, refresh y logout publicos.
 * - Catalogo de productos y categorias de lectura publica.
 * - Gestion de productos, categorias y usuarios:
 *   exclusiva del ADMINISTRADOR.
 * - Boletas protegidas segun el rol del usuario.
 * - Sesiones sin estado (STATELESS).
 * - Contrasenas cifradas mediante BCrypt.
 *
 * IMPORTANTE:
 * Las validaciones de propiedad de boletas deben realizarse
 * tambien en el backend para impedir accesos entre clientes.
 */

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // ======================================
    // FILTRO DE AUTENTICACION JWT
    // ======================================

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtFilter) {

        this.jwtFilter = jwtFilter;
    }

    // ======================================
    // CONFIGURACION DE SEGURIDAD HTTP
    // ======================================

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http) throws Exception {

        http
            .cors(Customizer.withDefaults())

            // API REST con JWT.
            // Revisar proteccion CSRF de endpoints
            // que utilizan cookies HttpOnly.
            .csrf(AbstractHttpConfigurer::disable)

            // No mantener sesiones HTTP
            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            // ==================================
            // AUTORIZACION POR RUTAS
            // ==================================

            .authorizeHttpRequests(auth -> auth

                // ==============================
                // AUTENTICACION
                // ==============================

                .requestMatchers(
                    "/api/v1/auth/**"
                ).permitAll()

                // ==============================
                // SWAGGER
                // ==============================

                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()

                // ==============================
                // PRODUCTOS
                // ==============================

                // Stock critico: ADMINISTRADOR
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/v1/productos/stock-critico"
                ).hasRole("ADMINISTRADOR")

                // Catalogo publico: solo GET
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/v1/productos",
                    "/api/v1/productos/**"
                ).permitAll()

                // Crear productos
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/v1/productos",
                    "/api/v1/productos/**"
                ).hasRole("ADMINISTRADOR")

                // Modificar productos
                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/v1/productos/**"
                ).hasRole("ADMINISTRADOR")

                // Eliminar productos
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/v1/productos/**"
                ).hasRole("ADMINISTRADOR")

                // ==============================
                // CATEGORIAS
                // ==============================

                // Consultar categorias: publico
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/v1/categorias",
                    "/api/v1/categorias/**"
                ).permitAll()

                // Crear categorias
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/v1/categorias",
                    "/api/v1/categorias/**"
                ).hasRole("ADMINISTRADOR")

                // Modificar categorias
                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/v1/categorias/**"
                ).hasRole("ADMINISTRADOR")

                // Eliminar categorias
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/v1/categorias/**"
                ).hasRole("ADMINISTRADOR")

                // ==============================
                // USUARIOS
                // ==============================

                // Gestion exclusiva del administrador
                .requestMatchers(
                    "/api/v1/usuarios",
                    "/api/v1/usuarios/**"
                ).hasRole("ADMINISTRADOR")

                // ==============================
                // BOLETAS / ORDENES
                // ==============================

                // Crear boleta:
                // ADMINISTRADOR y CLIENTE
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/v1/boletas"
                ).hasAnyRole(
                    "ADMINISTRADOR",
                    "CLIENTE"
                )

                // Listar y consultar boletas:
                // ADMINISTRADOR y VENDEDOR pueden consultar todas.
                // CLIENTE solo puede consultar sus propias boletas.
                // BoletaSecurityService valida la propiedad.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/v1/boletas",
                    "/api/v1/boletas/**"
                ).hasAnyRole(
                    "ADMINISTRADOR",
                    "VENDEDOR",
                    "CLIENTE"
                )

                // Cambiar estado de boleta:
                // solo ADMINISTRADOR
                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/v1/boletas/*/estado"
                ).hasRole("ADMINISTRADOR")

                // Bloquear otras operaciones
                // sobre boletas no autorizadas
                .requestMatchers(
                    "/api/v1/boletas",
                    "/api/v1/boletas/**"
                ).denyAll()

                // ==============================
                // RESTO DE ENDPOINTS
                // ==============================

                .anyRequest().authenticated()
            )

            // ==================================
            // ACTIVAR FILTRO JWT
            // ==================================

            .addFilterBefore(
                jwtFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    // ======================================
    // CIFRADO DE CONTRASENAS
    // ======================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
