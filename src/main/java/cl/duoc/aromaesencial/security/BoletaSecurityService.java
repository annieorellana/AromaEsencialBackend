
package cl.duoc.aromaesencial.security;

import cl.duoc.aromaesencial.dto.BoletaResponse;
import cl.duoc.aromaesencial.model.Usuario;
import cl.duoc.aromaesencial.repository.UsuarioRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("boletaSecurity")
public class BoletaSecurityService {

    private final UsuarioRepository usuarioRepository;

    public BoletaSecurityService(
            UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Obtener el usuario autenticado desde Spring Security
    private Usuario obtenerUsuarioAutenticado() {

        Authentication autenticacion =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (autenticacion == null
                || !autenticacion.isAuthenticated()
                || "anonymousUser".equals(
                        autenticacion.getPrincipal())) {

            throw new AuthenticationCredentialsNotFoundException(
                    "Debes iniciar sesión."
            );
        }

        String correo = autenticacion.getName();

        return usuarioRepository
                .findByCorreoIgnoreCase(correo)
                .filter(usuario ->
                        Boolean.TRUE.equals(usuario.getActivo()))
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Usuario no autorizado."
                        )
                );
    }

    // Comprobar si el usuario tiene el rol indicado
    private boolean tieneRol(
            Usuario usuario,
            String rol) {

        return usuario.getRol() != null
                && rol.equals(usuario.getRol().name());
    }

    // Comprobar si puede operar sobre un usuario
    public boolean puedeAccederAUsuario(Long usuarioId) {

        Usuario usuario = obtenerUsuarioAutenticado();

        if (tieneRol(usuario, "ADMINISTRADOR")) {
            return true;
        }

        return tieneRol(usuario, "CLIENTE")
                && usuarioId != null
                && usuario.getId().equals(usuarioId);
    }

    // Validar propiedad al crear una boleta
    public void validarCreacion(Long usuarioId) {

        if (!puedeAccederAUsuario(usuarioId)) {
            throw new AccessDeniedException(
                    "No puedes crear boletas para otro usuario."
            );
        }
    }

    // Validar propiedad al listar boletas de un cliente
    public void validarConsultaCliente(Long usuarioId) {

        if (!puedeAccederAUsuario(usuarioId)) {
            throw new AccessDeniedException(
                    "No puedes consultar boletas de otro usuario."
            );
        }
    }

// ======================================
// VALIDAR LISTADO DE BOLETAS
// ======================================

public void validarListado(Long usuarioId) {

    Usuario usuario = obtenerUsuarioAutenticado();

    // Administrador y vendedor pueden consultar.
    if (tieneRol(usuario, "ADMINISTRADOR")
            || tieneRol(usuario, "VENDEDOR")) {
        return;
    }

    // Cliente solamente consulta sus boletas.
    if (tieneRol(usuario, "CLIENTE")
            && usuarioId != null
            && usuario.getId().equals(usuarioId)) {
        return;
    }

    throw new AccessDeniedException(
        "No tienes permisos para consultar estas boletas."
    );
}

// ======================================
// VALIDAR DETALLE DE UNA BOLETA
// ======================================

public void validarDetalle(BoletaResponse boleta) {

    Usuario usuario = obtenerUsuarioAutenticado();

    // Administrador y vendedor:
    // acceso de lectura.
    if (tieneRol(usuario, "ADMINISTRADOR")
            || tieneRol(usuario, "VENDEDOR")) {
        return;
    }

    // Cliente: únicamente su propia boleta.
    if (tieneRol(usuario, "CLIENTE")
            && boleta != null
            && usuario.getId().equals(boleta.usuarioId())) {
        return;
    }

    throw new AccessDeniedException(
        "No tienes permisos para consultar esta boleta."
    );
}

}
