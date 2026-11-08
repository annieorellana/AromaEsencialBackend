package cl.duoc.aromaesencial.controller;

import java.util.List;

import cl.duoc.aromaesencial.config.ApiPaths;
import cl.duoc.aromaesencial.dto.CategoriaRequest;
import cl.duoc.aromaesencial.dto.CategoriaResponse;
import cl.duoc.aromaesencial.service.CategoriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CRUD de categorías.
 *
 * TODO (Parte 4): GET públicos; POST/PUT/DELETE solo ADMINISTRADOR (se define en SecurityConfig o con
 * &#64;PreAuthorize("hasRole('ADMINISTRADOR')")).
 */
@RestController
@RequestMapping(ApiPaths.CATEGORIAS)
@Tag(name = "Categorías", description = "Gestión de las categorías de perfumes (Hombre, Mujer, Unisex...)")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @Operation(summary = "Lista todas las categorías ordenadas por nombre")
    @ApiResponse(responseCode = "200", description = "Lista de categorías")
    public List<CategoriaResponse> listar() {
        return categoriaService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una categoría por su id")
    @ApiResponse(responseCode = "200", description = "Categoría encontrada")
    @ApiResponse(responseCode = "404", description = "La categoría no existe")
    public CategoriaResponse obtener(@PathVariable("id") Long id) {
        return categoriaService.obtener(id);
    }

    @PostMapping
    @Operation(summary = "Crea una categoría")
    @ApiResponse(responseCode = "201", description = "Categoría creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "409", description = "Ya existe una categoría con ese nombre")
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.crear(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza una categoría")
    @ApiResponse(responseCode = "200", description = "Categoría actualizada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "La categoría no existe")
    @ApiResponse(responseCode = "409", description = "Ya existe otra categoría con ese nombre")
    public CategoriaResponse actualizar(@PathVariable("id") Long id, @Valid @RequestBody CategoriaRequest request) {
        return categoriaService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina una categoría (solo si no tiene productos)")
    @ApiResponse(responseCode = "204", description = "Categoría eliminada")
    @ApiResponse(responseCode = "404", description = "La categoría no existe")
    @ApiResponse(responseCode = "409", description = "La categoría tiene productos asociados")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
