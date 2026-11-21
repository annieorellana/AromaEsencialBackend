
package cl.duoc.aromaesencial.service;

import cl.duoc.aromaesencial.model.RefreshToken;
import cl.duoc.aromaesencial.model.Usuario;
import cl.duoc.aromaesencial.repository.RefreshTokenRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repositorio;
    private final SecureRandom random = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository repositorio) {
        this.repositorio = repositorio;
    }

    // Generar un refresh token aleatorio
    @Transactional
    public String crearToken(Usuario usuario) {

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        RefreshToken entidad = new RefreshToken();
        entidad.setTokenHash(calcularHash(token));
        entidad.setUsuario(usuario);
        entidad.setFechaExpiracion(
                Instant.now().plus(7, ChronoUnit.DAYS)
        );
        entidad.setRevocado(false);

        repositorio.save(entidad);

        return token;
    }

    // Validar un refresh token
    @Transactional(readOnly = true)
    public Usuario validarToken(String token) {

        RefreshToken entidad = repositorio
                .findByTokenHash(calcularHash(token))
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Refresh token inválido."
                        )
                );

        if (entidad.isRevocado()
                || !entidad.getFechaExpiracion()
                        .isAfter(Instant.now())
                || !Boolean.TRUE.equals(
                        entidad.getUsuario().getActivo())) {

            throw new IllegalArgumentException(
                    "Refresh token expirado o revocado."
            );
        }

        return entidad.getUsuario();
    }

    // Revocar un refresh token
    @Transactional
    public void revocarToken(String token) {

        repositorio.findByTokenHash(calcularHash(token))
                .ifPresent(entidad -> {
                    entidad.setRevocado(true);
                    repositorio.save(entidad);
                });
    }

    // Resultado de una renovación exitosa
    public record ResultadoRotacion(
            Usuario usuario,
            String nuevoRefreshToken
    ) {}

    // Renovar el refresh token de forma transaccional
    @Transactional
    public ResultadoRotacion rotarToken(String tokenActual) {

        if (tokenActual == null || tokenActual.isBlank()) {
            throw new IllegalArgumentException(
                    "Refresh token obligatorio."
            );
        }

        // Bloquear el registro mientras se renueva
        RefreshToken anterior = repositorio
                .buscarParaRotacion(calcularHash(tokenActual))
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Refresh token inválido."
                        )
                );

        // Verificar vencimiento y revocación
        if (anterior.isRevocado()
                || !anterior.getFechaExpiracion()
                        .isAfter(Instant.now())) {

            throw new IllegalArgumentException(
                    "Refresh token expirado o revocado."
            );
        }

        Usuario usuario = anterior.getUsuario();

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new IllegalArgumentException(
                    "Usuario inactivo."
            );
        }

        // Invalidar el refresh token anterior
        anterior.setRevocado(true);
        repositorio.save(anterior);

        // Crear uno nuevo dentro de la misma transacción
        String nuevoToken = crearToken(usuario);

        return new ResultadoRotacion(
                usuario,
                nuevoToken
        );
    }


    // Calcular SHA-256 para no guardar el token original
    private String calcularHash(String token) {

        try {
            byte[] hash = MessageDigest
                    .getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "No fue posible calcular el hash.",
                    ex
            );
        }
    }
}
