
package cl.duoc.aromaesencial.service;

import java.util.List;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.aromaesencial.dto.UsuarioRequest;
import cl.duoc.aromaesencial.dto.UsuarioResponse;
import cl.duoc.aromaesencial.model.Rol;
import cl.duoc.aromaesencial.model.Usuario;
import cl.duoc.aromaesencial.repository.BoletaRepository;
import cl.duoc.aromaesencial.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BoletaRepository boletaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            BoletaRepository boletaRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.boletaRepository = boletaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ==========================================
    // LISTAR USUARIOS
    // ==========================================

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {

        return usuarioRepository.findAll()
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    // ==========================================
    // BUSCAR USUARIO POR ID
    // ==========================================

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {

        Usuario usuario = obtenerUsuario(id);

        return convertirRespuesta(usuario);
    }

    // ==========================================
    // CREAR USUARIO
    // ==========================================

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {

        String run = normalizarRun(request.run());
        String correo = normalizarCorreo(request.correo());

        if (usuarioRepository.existsByRun(run)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El RUN ya está registrado"
            );
        }

        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El correo ya está registrado"
            );
        }

        if (request.password() == null ||
                request.password().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña es obligatoria"
            );
        }

        Usuario usuario = new Usuario();

        asignarDatos(usuario, request, run, correo);

        // La contraseña nunca se guarda en texto plano.
        usuario.setPassword(
                passwordEncoder.encode(request.password())
        );

        usuario.setRol(
                request.rol() != null
                        ? request.rol()
                        : Rol.CLIENTE
        );

        usuario.setActivo(
                request.activo() != null
                        ? request.activo()
                        : true
        );

        Usuario guardado = usuarioRepository.save(usuario);

        return convertirRespuesta(guardado);
    }

    // ==========================================
    // ACTUALIZAR USUARIO
    // ==========================================

    @Transactional
    public UsuarioResponse actualizar(
            Long id,
            UsuarioRequest request) {

        Usuario usuario = obtenerUsuario(id);

        String nuevoRun = normalizarRun(request.run());
        String nuevoCorreo = normalizarCorreo(request.correo());

        // ======================================
        // VALIDAR RUN DUPLICADO
        // ======================================

        if (!normalizarRun(usuario.getRun()).equals(nuevoRun)
                && usuarioRepository.existsByRun(nuevoRun)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El RUN ya pertenece a otro usuario"
            );
        }

        // ======================================
        // VALIDAR CORREO DUPLICADO
        // ======================================

        if (!usuario.getCorreo().equalsIgnoreCase(nuevoCorreo)
                && usuarioRepository.existsByCorreoIgnoreCase(nuevoCorreo)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El correo ya pertenece a otro usuario"
            );
        }

        // Si el formulario no envía rol o estado,
        // conservar los valores actuales.
        Rol nuevoRol = request.rol() != null
                ? request.rol()
                : usuario.getRol();

        boolean nuevoEstado = request.activo() != null
                ? request.activo()
                : Boolean.TRUE.equals(usuario.getActivo());

        // ======================================
        // PROTEGER CUENTA ADMIN ACTUAL
        // ======================================

        if (esUsuarioAutenticado(usuario)) {

            if (nuevoRol != Rol.ADMINISTRADOR || !nuevoEstado) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "No puedes quitarte el rol administrador "
                        + "ni desactivar tu propia cuenta"
                );
            }
        }

        // ======================================
        // PROTEGER ÚLTIMO ADMIN ACTIVO
        // ======================================

        boolean pierdeAccesoAdministrador =
                usuario.getRol() == Rol.ADMINISTRADOR
                && Boolean.TRUE.equals(usuario.getActivo())
                && (
                    nuevoRol != Rol.ADMINISTRADOR
                    || !nuevoEstado
                );

        if (pierdeAccesoAdministrador) {
            protegerUltimoAdministrador();
        }

        // ======================================
        // ACTUALIZAR DATOS
        // ======================================

        asignarDatos(usuario, request, nuevoRun, nuevoCorreo);

        // Una contraseña vacía o nula conserva
        // la contraseña anterior.
        if (request.password() != null
                && !request.password().isBlank()) {

            usuario.setPassword(
                    passwordEncoder.encode(request.password())
            );
        }

        usuario.setRol(nuevoRol);
        usuario.setActivo(nuevoEstado);

        Usuario actualizado = usuarioRepository.save(usuario);

        return convertirRespuesta(actualizado);
    }

    // ==========================================
    // ELIMINAR USUARIO
    // ==========================================

    @Transactional
    public void eliminar(Long id) {

        Usuario usuario = obtenerUsuario(id);

        // ======================================
        // IMPEDIR AUTOELIMINACIÓN
        // ======================================

        if (esUsuarioAutenticado(usuario)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes eliminar tu propia cuenta"
            );
        }

        // ======================================
        // PROTEGER ÚLTIMO ADMIN ACTIVO
        // ======================================

        if (usuario.getRol() == Rol.ADMINISTRADOR
                && Boolean.TRUE.equals(usuario.getActivo())) {

            protegerUltimoAdministrador();
        }

        // ======================================
        // CONSERVAR HISTORIAL DE COMPRAS
        // ======================================

        boolean tieneBoletas =
                !boletaRepository
                    .findByUsuarioIdOrderByFechaDesc(id)
                    .isEmpty();

        if (tieneBoletas) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este usuario tiene boletas asociadas. "
                    + "Desactívalo en lugar de eliminarlo "
                    + "para conservar el historial de compras."
            );
        }

        // ======================================
        // ELIMINAR SOLO SI NO HAY RELACIONES
        // QUE IMPIDAN LA OPERACIÓN
        // ======================================

        try {

            usuarioRepository.delete(usuario);

            // Ejecutar la eliminación dentro de
            // la transacción para detectar errores
            // de integridad referencial.
            usuarioRepository.flush();

        } catch (DataIntegrityViolationException ex) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar este usuario "
                    + "porque existen registros asociados. "
                    + "Puedes desactivarlo para conservar "
                    + "su información.",
                    ex
            );
        }
    }

    // ==========================================
    // PROTEGER ÚLTIMO ADMINISTRADOR ACTIVO
    // ==========================================

    private void protegerUltimoAdministrador() {

        long administradoresActivos =
                usuarioRepository.findByRol(Rol.ADMINISTRADOR)
                        .stream()
                        .filter(usuario ->
                                Boolean.TRUE.equals(usuario.getActivo()))
                        .count();

        if (administradoresActivos <= 1) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La operación dejaría al sistema "
                    + "sin administradores activos"
            );
        }
    }

    // ==========================================
    // COMPROBAR USUARIO AUTENTICADO
    // ==========================================

    private boolean esUsuarioAutenticado(Usuario usuario) {

        Authentication autenticacion =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (autenticacion == null
                || !autenticacion.isAuthenticated()) {
            return false;
        }

        String correoAutenticado = autenticacion.getName();

        return correoAutenticado != null
                && usuario.getCorreo() != null
                && usuario.getCorreo()
                        .equalsIgnoreCase(correoAutenticado);
    }

    // ==========================================
    // OBTENER USUARIO O DEVOLVER 404
    // ==========================================

    private Usuario obtenerUsuario(Long id) {

        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Usuario no encontrado"
                        )
                );
    }

    // ==========================================
    // NORMALIZAR RUN
    // ==========================================

    private String normalizarRun(String run) {

        if (run == null || run.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El RUN es obligatorio"
            );
        }

        return run.trim()
                .replaceAll("[.\\-\\s]", "")
                .toUpperCase(Locale.ROOT);
    }

    // ==========================================
    // NORMALIZAR CORREO
    // ==========================================

    private String normalizarCorreo(String correo) {

        if (correo == null || correo.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El correo es obligatorio"
            );
        }

        return correo.trim()
                .toLowerCase(Locale.ROOT);
    }

    // ==========================================
    // ASIGNAR DATOS DEL FORMULARIO
    // ==========================================

    private void asignarDatos(
            Usuario usuario,
            UsuarioRequest request,
            String run,
            String correo) {

        usuario.setRun(run);

        usuario.setNombre(request.nombre().trim());
        usuario.setApellido(request.apellido().trim());

        usuario.setCorreo(correo);
        usuario.setTelefono(request.telefono());

        usuario.setFechaNacimiento(
                request.fechaNacimiento()
        );

        usuario.setRegion(request.region());
        usuario.setComuna(request.comuna());
        usuario.setDireccion(request.direccion());
    }

    // ==========================================
    // CONVERTIR A DTO SIN CONTRASEÑA
    // ==========================================

    private UsuarioResponse convertirRespuesta(Usuario usuario) {

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getRun(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getCorreo(),
                usuario.getTelefono(),
                usuario.getFechaNacimiento(),
                usuario.getRegion(),
                usuario.getComuna(),
                usuario.getDireccion(),
                usuario.getRol(),
                usuario.getActivo()
        );
    }
}
