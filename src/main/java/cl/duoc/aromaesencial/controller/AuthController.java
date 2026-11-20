
package cl.duoc.aromaesencial.controller;

import cl.duoc.aromaesencial.dto.AuthResponse;
import cl.duoc.aromaesencial.dto.LoginRequest;
import cl.duoc.aromaesencial.dto.RefreshResponse;
import cl.duoc.aromaesencial.dto.RegistroRequest;

import cl.duoc.aromaesencial.model.Rol;
import cl.duoc.aromaesencial.model.Usuario;

import cl.duoc.aromaesencial.repository.UsuarioRepository;

import cl.duoc.aromaesencial.security.JwtService;
import cl.duoc.aromaesencial.service.RefreshTokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.bind.annotation.*;

/**
 * ============================================================
 * AROMAESENCIAL - AUTENTICACION Y SEGURIDAD
 * INTEGRANTE B
 * ============================================================
 *
 * Funcionalidades:
 *
 * 1. Login mediante JWT.
 * 2. Registro de usuarios con rol CLIENTE.
 * 3. Cifrado de contrasenas con BCrypt.
 * 4. Renovacion de JWT mediante refresh token.
 * 5. Rotacion y revocacion de refresh tokens.
 * 6. Logout con eliminacion de cookie.
 * 7. Cookie HttpOnly, SameSite=Lax.
 * 8. Validacion de origen para refresh y logout.
 *
 * La validacion de origen es una defensa adicional
 * y no sustituye una estrategia CSRF completa.
 */

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    // ======================================
    // DEPENDENCIAS
    // ======================================

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    // ======================================
    // CONFIGURACION
    // ======================================

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.cors.allowed-origins}")
    private String origenesPermitidos;

    @Value("${app.auth.swagger-origin:http://localhost:8080}")
    private String origenSwagger;

    // ======================================
    // CONSTRUCTOR
    // ======================================

    public AuthController(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    // ======================================
    // LOGIN
    // POST /api/v1/auth/login
    // ======================================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest datos) {

        Optional<Usuario> encontrado =
                usuarioRepository.findByCorreoIgnoreCase(
                        datos.correo()
                );

        if (encontrado.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Correo o contraseña incorrectos");
        }

        Usuario usuario = encontrado.get();

        // Comprobar que la cuenta este activa
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Correo o contraseña incorrectos");
        }

        // Verificar contrasena mediante BCrypt
        if (!passwordEncoder.matches(
                datos.password(),
                usuario.getPassword())) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Correo o contraseña incorrectos");
        }

        // Generar JWT
        String token = jwtService.generarToken(usuario);

        AuthResponse respuesta = new AuthResponse(
                token,
                "Bearer",
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol().name()
        );

        // Generar refresh token aleatorio
        String refreshToken =
                refreshTokenService.crearToken(usuario);

        // Entregar JWT y cookie HttpOnly
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        crearCookieRefresh(refreshToken).toString()
                )
                .body(respuesta);
    }

    // ======================================
    // REGISTRO
    // POST /api/v1/auth/registro
    // ======================================

    @PostMapping("/registro")
    public ResponseEntity<?> registro(
            @Valid @RequestBody RegistroRequest datos) {

        String correo = datos.correo()
                .trim()
                .toLowerCase();

        String run = datos.run()
                .replace(".", "")
                .replace("-", "")
                .trim()
                .toUpperCase();

        // Comprobar correo duplicado
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("El correo ya está registrado.");
        }

        // Comprobar RUN duplicado
        if (usuarioRepository.existsByRun(run)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("El RUN ya está registrado.");
        }

        // BCrypt admite hasta 72 bytes
        int bytes = datos.password()
                .getBytes(StandardCharsets.UTF_8)
                .length;

        if (bytes > 72) {
            return ResponseEntity
                    .badRequest()
                    .body(
                        "La contraseña supera los 72 bytes permitidos."
                    );
        }

        // Crear usuario
        Usuario usuario = new Usuario();

        usuario.setRun(run);
        usuario.setNombre(datos.nombre().trim());
        usuario.setApellido(datos.apellido().trim());
        usuario.setCorreo(correo);

        // Guardar contrasena cifrada
        usuario.setPassword(
                passwordEncoder.encode(datos.password())
        );

        usuario.setTelefono(datos.telefono());
        usuario.setFechaNacimiento(datos.fechaNacimiento());

        usuario.setRegion(datos.region());
        usuario.setComuna(datos.comuna());
        usuario.setDireccion(datos.direccion());

        // El registro publico crea solo CLIENTES
        usuario.setRol(Rol.CLIENTE);
        usuario.setActivo(true);

        // Guardar en MySQL
        Usuario guardado =
                usuarioRepository.save(usuario);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "mensaje",
                        "Usuario registrado correctamente.",
                        "id",
                        guardado.getId(),
                        "correo",
                        guardado.getCorreo(),
                        "rol",
                        guardado.getRol().name()
                ));
    }

    // ======================================
    // RENOVAR SESION
    // POST /api/v1/auth/refresh
    // ======================================

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(
                    name = "refreshToken",
                    required = false
            ) String tokenActual,
            HttpServletRequest request) {

        // Validar origen de la solicitud
        if (!origenPermitido(request)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Origen de solicitud no autorizado.");
        }

        // Comprobar que exista refresh token
        if (tokenActual == null || tokenActual.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("No existe una sesión renovable.");
        }

        try {

            // Revocar el token anterior y generar uno nuevo
            RefreshTokenService.ResultadoRotacion resultado =
                    refreshTokenService.rotarToken(tokenActual);

            // Generar nuevo JWT de acceso
            String nuevoJwt =
                    jwtService.generarToken(resultado.usuario());

            RefreshResponse respuesta =
                    new RefreshResponse(
                            nuevoJwt,
                            "Bearer"
                    );

            // Actualizar cookie de renovacion
            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.SET_COOKIE,
                            crearCookieRefresh(
                                    resultado.nuevoRefreshToken()
                            ).toString()
                    )
                    .body(respuesta);

        } catch (IllegalArgumentException ex) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("La sesión no se puede renovar.");
        }
    }

    // ======================================
    // CERRAR SESION
    // POST /api/v1/auth/logout
    // ======================================

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(
                    name = "refreshToken",
                    required = false
            ) String tokenActual,
            HttpServletRequest request) {

        // Validar origen de la solicitud
        if (!origenPermitido(request)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Origen de solicitud no autorizado.");
        }

        // Revocar refresh token en MySQL
        if (tokenActual != null && !tokenActual.isBlank()) {
            refreshTokenService.revocarToken(tokenActual);
        }

        // Eliminar cookie del navegador
        return ResponseEntity
                .noContent()
                .header(
                        HttpHeaders.SET_COOKIE,
                        eliminarCookieRefresh().toString()
                )
                .build();
    }

    // ======================================
    // VALIDACION DE ORIGEN
    // ======================================

    private boolean origenPermitido(
            HttpServletRequest request) {

        String origen = request.getHeader("Origin");

        // Si falta Origin, intentar con Referer
        if (origen == null || origen.isBlank()) {

            String referer = request.getHeader("Referer");

            if (referer == null || referer.isBlank()) {
                return false;
            }

            try {

                URI uri = URI.create(referer);

                if (uri.getScheme() == null
                        || uri.getHost() == null) {
                    return false;
                }

                origen = uri.getScheme()
                        + "://" + uri.getHost();

                if (uri.getPort() != -1) {
                    origen += ":" + uri.getPort();
                }

            } catch (IllegalArgumentException ex) {
                return false;
            }
        }

        final String origenSolicitud = origen;

        // Permitir Swagger local
        if (origenSwagger.equals(origenSolicitud)) {
            return true;
        }

        // Permitir los origenes configurados para React
        return Arrays.stream(origenesPermitidos.split(","))
                .map(String::trim)
                .filter(valor -> !valor.isEmpty())
                .anyMatch(origenSolicitud::equals);
    }

    // ======================================
    // CREAR COOKIE HTTPONLY
    // ======================================

    private ResponseCookie crearCookieRefresh(
            String token) {

        return ResponseCookie
                .from("refreshToken", token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(Duration.ofDays(7))
                .build();
    }

    // ======================================
    // ELIMINAR COOKIE HTTPONLY
    // ======================================

    private ResponseCookie eliminarCookieRefresh() {

        return ResponseCookie
                .from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(Duration.ZERO)
                .build();
    }
}
