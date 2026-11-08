package cl.duoc.aromaesencial.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ============================================================
 *  CONFIGURACIÓN DE SEGURIDAD - BASE TEMPORAL (Parte 1)
 * ============================================================
 * Por ahora TODO está permitido (permitAll) para que las Partes 2 y 3 puedan desarrollar y probar sus
 * endpoints en Swagger sin token.
 *
 * La Parte 4 (autenticación/JWT) debe reemplazar la regla {@code anyRequest().permitAll()} por:
 *   - rutas públicas: /api/v1/auth/**, GET /api/v1/productos/**, GET /api/v1/categorias/**, Swagger
 *   - ADMINISTRADOR: acceso total
 *   - VENDEDOR: solo GET de productos y GET de boletas
 *   - CLIENTE: solo tienda (crear/consultar sus propias boletas)
 * y agregar el filtro JWT antes de UsernamePasswordAuthenticationFilter.
 *
 * Ya están listos: CORS, sesión sin estado (stateless), CSRF desactivado (API con token),
 * PasswordEncoder (BCrypt) y &#64;EnableMethodSecurity para usar &#64;PreAuthorize en los controladores.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // TODO (Parte 4): reemplazar por reglas reales por rol + filtro JWT
                        .anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
