package cl.duoc.aromaesencial.controller;

import java.util.List;

import cl.duoc.aromaesencial.config.ApiPaths;
import cl.duoc.aromaesencial.dto.ProductoRequest;
import cl.duoc.aromaesencial.dto.ProductoResponse;
import cl.duoc.aromaesencial.service.ProductoService;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * CRUD del catálogo de productos.
 *
 * TODO (Parte 4): GET (listar/detalle/ofertas) públicos; /stock-critico y POST/PUT/DELETE solo ADMINISTRADOR;
 * el VENDEDOR solo puede consultar (GET) productos.
 */
@RestController
@RequestMapping(ApiPaths.PRODUCTOS)
@Tag(name = "Productos", description = "Gestión del catálogo de perfumes")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Lista productos",
            description = "Filtros opcionales y combinables: categoriaId (exacto) y buscar (texto contenido en el nombre).")
    @ApiResponse(responseCode = "200", description = "Lista de productos")
    public List<ProductoResponse> listar(
            @RequestParam(name = "categoriaId", required = false) Long categoriaId,
            @RequestParam(name = "buscar", required = false) String buscar) {
        return productoService.listar(categoriaId, buscar);
    }

    @GetMapping("/ofertas")
    @Operation(summary = "Lista los productos en oferta")
    @ApiResponse(responseCode = "200", description = "Productos con oferta activa")
    public List<ProductoResponse> ofertas() {
        return productoService.listarOfertas();
    }

    @GetMapping("/stock-critico")
    @Operation(summary = "Lista los productos con stock crítico (stock <= stockCritico)")
    @ApiResponse(responseCode = "200", description = "Productos con stock crítico, del más bajo al más alto")
    public List<ProductoResponse> stockCritico() {
        return productoService.listarStockCritico();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene el detalle de un producto")
    @ApiResponse(responseCode = "200", description = "Producto encontrado")
    @ApiResponse(responseCode = "404", description = "El producto no existe")
    public ProductoResponse obtener(@PathVariable("id") Long id) {
        return productoService.obtener(id);
    }

    @PostMapping
    @Operation(summary = "Crea un producto")
    @ApiResponse(responseCode = "201", description = "Producto creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "La categoría indicada no existe")
    @ApiResponse(responseCode = "409", description = "Ya existe un producto con ese código")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un producto")
    @ApiResponse(responseCode = "200", description = "Producto actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "El producto o la categoría no existen")
    @ApiResponse(responseCode = "409", description = "Ya existe otro producto con ese código")
    public ProductoResponse actualizar(@PathVariable("id") Long id, @Valid @RequestBody ProductoRequest request) {
        return productoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un producto (solo si nunca fue vendido)")
    @ApiResponse(responseCode = "204", description = "Producto eliminado")
    @ApiResponse(responseCode = "404", description = "El producto no existe")
    @ApiResponse(responseCode = "409", description = "El producto aparece en boletas")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
