
package cl.duoc.aromaesencial.controller;

import cl.duoc.aromaesencial.model.Usuario;
import cl.duoc.aromaesencial.repository.UsuarioRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(
            UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<UsuarioResumen>> listarUsuarios() {

        List<UsuarioResumen> usuarios =
                usuarioRepository.findAll()
                    .stream()
                    .map(u -> new UsuarioResumen(
                            u.getId(),
                            u.getNombre(),
                            u.getApellido(),
                            u.getCorreo(),
                            u.getRol().name(),
                            u.getActivo()
                    ))
                    .toList();

        return ResponseEntity.ok(usuarios);
    }

    public record UsuarioResumen(
            Long id,
            String nombre,
            String apellido,
            String correo,
            String rol,
            Boolean activo
    ) {}
}
