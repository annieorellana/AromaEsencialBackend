package cl.duoc.aromaesencial.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cl.duoc.aromaesencial.config.ApiPaths;
import cl.duoc.aromaesencial.dto.UsuarioRequest;
import cl.duoc.aromaesencial.dto.UsuarioResponse;
import cl.duoc.aromaesencial.service.UsuarioService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping(ApiPaths.USUARIOS)
@Validated
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // GET: LISTAR USUARIOS
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {

        return ResponseEntity.ok(usuarioService.listar());
    }

    // GET: BUSCAR POR ID
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(
            @PathVariable @Positive Long id) {

        return ResponseEntity.ok(
                usuarioService.buscarPorId(id)
        );
    }

    // POST: CREAR USUARIO
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(
            @Valid @RequestBody UsuarioRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioService.crear(request));
    }

    // PUT: ACTUALIZAR USUARIO
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UsuarioRequest request) {

        return ResponseEntity.ok(
                usuarioService.actualizar(id, request)
        );
    }

    // DELETE: ELIMINAR USUARIO
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable @Positive Long id) {

        usuarioService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}
