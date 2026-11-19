
package cl.duoc.aromaesencial.security;

import cl.duoc.aromaesencial.model.Usuario;
import cl.duoc.aromaesencial.repository.UsuarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UsuarioRepository usuarioRepository) {

        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null
                && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext()
                    .getAuthentication() == null) {

            String token = header.substring(7);

            try {
                String correo = jwtService.obtenerCorreo(token);

                Optional<Usuario> usuario =
                    usuarioRepository.findByCorreoIgnoreCase(correo);

                if (usuario.isPresent()
                        && jwtService.validarToken(
                            token, usuario.get())) {

                    Usuario encontrado = usuario.get();

                    String autoridad =
                        "ROLE_" + encontrado.getRol().name();

                    var permisos = List.of(
                        new SimpleGrantedAuthority(autoridad)
                    );

                    var autenticacion =
                        new UsernamePasswordAuthenticationToken(
                            encontrado.getCorreo(),
                            null,
                            permisos
                        );

                    SecurityContextHolder.getContext()
                        .setAuthentication(autenticacion);
                }

            } catch (RuntimeException ex) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
