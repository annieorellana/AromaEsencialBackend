package cl.duoc.aromaesencial.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.aromaesencial.dto.UsuarioRequest;
import cl.duoc.aromaesencial.dto.UsuarioResponse;
import cl.duoc.aromaesencial.model.Rol;
import cl.duoc.aromaesencial.model.Usuario;
import cl.duoc.aromaesencial.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // LISTAR TODOS LOS USUARIOS
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {

        return usuarioRepository.findAll()
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    // BUSCAR USUARIO POR ID
    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {

        Usuario usuario = obtenerUsuario(id);

        return convertirRespuesta(usuario);
    }

    // CREAR USUARIO
    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {

        if (usuarioRepository.existsByRun(request.run())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El RUN ya esta registrado"
            );
        }

        if (usuarioRepository.existsByCorreoIgnoreCase(request.correo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El correo ya esta registrado"
            );
        }

        if (request.password() == null ||
                request.password().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contrasena es obligatoria"
            );
        }

        Usuario usuario = new Usuario();

        asignarDatos(usuario, request);

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

    // ACTUALIZAR USUARIO
    @Transactional
    public UsuarioResponse actualizar(
            Long id,
            UsuarioRequest request) {

        Usuario usuario = obtenerUsuario(id);

        if (!usuario.getRun().equals(request.run()) &&
                usuarioRepository.existsByRun(request.run())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El RUN ya pertenece a otro usuario"
            );
        }

        if (!usuario.getCorreo().equalsIgnoreCase(request.correo()) &&
                usuarioRepository.existsByCorreoIgnoreCase(request.correo())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El correo ya pertenece a otro usuario"
            );
        }

        asignarDatos(usuario, request);

        // La contraseña solo cambia si se envía una nueva.
        if (request.password() != null &&
                !request.password().isBlank()) {

            usuario.setPassword(
                    passwordEncoder.encode(request.password())
            );
        }

        if (request.rol() != null) {
            usuario.setRol(request.rol());
        }

        if (request.activo() != null) {
            usuario.setActivo(request.activo());
        }

        Usuario actualizado = usuarioRepository.save(usuario);

        return convertirRespuesta(actualizado);
    }

    // ELIMINAR USUARIO
    @Transactional
    public void eliminar(Long id) {

        Usuario usuario = obtenerUsuario(id);

        usuarioRepository.delete(usuario);
    }

    // BUSCAR ENTIDAD Y CONTROLAR ERROR 404
    private Usuario obtenerUsuario(Long id) {

        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Usuario no encontrado"
                        )
                );
    }

    // ASIGNAR DATOS COMUNES
    private void asignarDatos(
            Usuario usuario,
            UsuarioRequest request) {

        usuario.setRun(request.run());
        usuario.setNombre(request.nombre());
        usuario.setApellido(request.apellido());
        usuario.setCorreo(request.correo());
        usuario.setTelefono(request.telefono());
        usuario.setFechaNacimiento(request.fechaNacimiento());
        usuario.setRegion(request.region());
        usuario.setComuna(request.comuna());
        usuario.setDireccion(request.direccion());
    }

    // CONVERTIR ENTIDAD A DTO SIN CONTRASEÑA
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