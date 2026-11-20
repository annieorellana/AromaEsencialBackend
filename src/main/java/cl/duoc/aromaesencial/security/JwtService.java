
package cl.duoc.aromaesencial.security;

import cl.duoc.aromaesencial.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secreto;

    @Value("${app.jwt.expiration-minutes}")
    private long expiracionMinutos;

    // Obtener la clave secreta
    private SecretKey obtenerClave() {
        return Keys.hmacShaKeyFor(
            secreto.getBytes(StandardCharsets.UTF_8)
        );
    }

    // Generar un token para un usuario
    public String generarToken(Usuario usuario) {

        Date ahora = new Date();

        Date vencimiento = new Date(
            ahora.getTime() + expiracionMinutos * 60_000L
        );

        return Jwts.builder()
            .subject(usuario.getCorreo())
            .claim("rol", usuario.getRol().name())
            .issuedAt(ahora)
            .expiration(vencimiento)
            .signWith(obtenerClave())
            .compact();
    }

    // Verificar token y obtener sus datos
    public Claims obtenerClaims(String token) {

        return Jwts.parser()
            .verifyWith(obtenerClave())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    // Obtener el correo del token
    public String obtenerCorreo(String token) {
        return obtenerClaims(token).getSubject();
    }

    // Validar que el token pertenezca al usuario
    public boolean validarToken(
        String token,
        Usuario usuario
    ) {
        try {
            Claims claims = obtenerClaims(token);

            return claims.getSubject()
                    .equalsIgnoreCase(usuario.getCorreo())
                && Boolean.TRUE.equals(usuario.getActivo())
                && claims.getExpiration().after(new Date());

        } catch (RuntimeException ex) {
            return false;
        }
    }
}
