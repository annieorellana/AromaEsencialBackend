
package cl.duoc.aromaesencial.controller;

import java.util.List;

import cl.duoc.aromaesencial.config.ApiPaths;
import cl.duoc.aromaesencial.dto.BoletaResponse;
import cl.duoc.aromaesencial.dto.CambiarEstadoBoletaRequest;
import cl.duoc.aromaesencial.dto.CrearBoletaRequest;
import cl.duoc.aromaesencial.service.BoletaService;
import cl.duoc.aromaesencial.security.BoletaSecurityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.BOLETAS)
@Tag(
    name = "Boletas",
    description = "Gestión de órdenes de compra"
)
public class BoletaController {

    private final BoletaService boletaService;
    private final BoletaSecurityService boletaSecurity;

    // ======================================
    // CONSTRUCTOR
    // ======================================

    public BoletaController(
            BoletaService boletaService,
            BoletaSecurityService boletaSecurity) {

        this.boletaService = boletaService;
        this.boletaSecurity = boletaSecurity;
    }

    // ======================================
    // CREAR BOLETA
    // POST /api/v1/boletas
    // ======================================

    @PostMapping
    @Operation(summary = "Crea una nueva boleta")
    @ApiResponse(
        responseCode = "201",
        description = "Boleta creada"
    )
    @ApiResponse(
        responseCode = "400",
        description = "Datos inválidos o regla de negocio incumplida"
    )
    @ApiResponse(
        responseCode = "403",
        description = "No autorizado para crear esta boleta"
    )
    @ApiResponse(
        responseCode = "404",
        description = "Usuario o producto no encontrado"
    )
    public ResponseEntity<BoletaResponse> crear(

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                required = true,
                description = "Datos necesarios para generar una nueva orden de compra",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                        implementation = CrearBoletaRequest.class
                    ),
                    examples = @ExampleObject(
                        name = "Ejemplo de compra",
                        value = """
                            {
                              "usuarioId": 3,
                              "items": [
                                {
                                  "productoId": 1,
                                  "cantidad": 2
                                }
                              ],
                              "cupon": "AROMA10",
                              "metodoPago": "Tarjeta",
                              "metodoEnvio": "domicilio",
                              "direccionEnvio": "Av. Principal 123",
                              "region": "Metropolitana",
                              "comuna": "Santiago",
                              "telefono": "912345678"
                            }
                            """
                    )
                )
            )

            @Valid @RequestBody CrearBoletaRequest request) {

        // SEGURIDAD:
        // El cliente solo puede comprar a su nombre.
        boletaSecurity.validarCreacion(request.usuarioId());

        // La lógica comercial sigue en BoletaService.
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(boletaService.crear(request));
    }

    // ======================================
    // LISTAR BOLETAS
    // GET /api/v1/boletas
    // GET /api/v1/boletas?usuarioId=3
    // ======================================

    @GetMapping
    @Operation(
        summary = "Lista las boletas",
        description = "Permite filtrar por usuarioId según permisos."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Lista de boletas"
    )
    @ApiResponse(
        responseCode = "403",
        description = "Acceso denegado"
    )
    public List<BoletaResponse> listar(
            @RequestParam(
                name = "usuarioId",
                required = false
            ) Long usuarioId) {

        // SEGURIDAD:
        // Administrador y vendedor pueden consultar.
        // Cliente debe indicar su propio usuarioId.
        boletaSecurity.validarListado(usuarioId);

        if (usuarioId != null) {
            return boletaService.listarPorUsuario(usuarioId);
        }

        return boletaService.listar();
    }

    // ======================================
    // CONSULTAR BOLETA POR ID
    // GET /api/v1/boletas/{id}
    // ======================================

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtiene el detalle de una boleta"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Boleta encontrada"
    )
    @ApiResponse(
        responseCode = "403",
        description = "No autorizado para consultar la boleta"
    )
    @ApiResponse(
        responseCode = "404",
        description = "La boleta no existe"
    )
    public BoletaResponse obtener(
            @PathVariable("id") Long id) {

        BoletaResponse boleta = boletaService.obtener(id);

        // SEGURIDAD:
        // Validar el propietario antes de devolver datos.
        boletaSecurity.validarDetalle(boleta);

        return boleta;
    }

    // ======================================
    // CAMBIAR ESTADO
    // PATCH /api/v1/boletas/{id}/estado
    // ======================================

    @PatchMapping("/{id}/estado")
    @Operation(
        summary = "Cambia el estado de una boleta"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Estado actualizado"
    )
    @ApiResponse(
        responseCode = "400",
        description = "Cambio de estado no permitido"
    )
    @ApiResponse(
        responseCode = "403",
        description = "Acceso denegado"
    )
    @ApiResponse(
        responseCode = "404",
        description = "La boleta no existe"
    )
    public BoletaResponse cambiarEstado(
            @PathVariable("id") Long id,
            @Valid @RequestBody CambiarEstadoBoletaRequest request) {

        // El acceso al endpoint lo controla
        // SecurityConfig: solo ADMINISTRADOR.

        return boletaService.cambiarEstado(id, request);
    }
}
