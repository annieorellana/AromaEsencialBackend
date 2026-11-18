package cl.duoc.aromaesencial.controller;

import java.util.List;

import cl.duoc.aromaesencial.config.ApiPaths;
import cl.duoc.aromaesencial.dto.BoletaResponse;
import cl.duoc.aromaesencial.dto.CambiarEstadoBoletaRequest;
import cl.duoc.aromaesencial.dto.CrearBoletaRequest;
import cl.duoc.aromaesencial.service.BoletaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Gestión de boletas / órdenes.
 *
 * TODO (Parte Seguridad):
 * - CLIENTE: crear y consultar solo sus propias boletas.
 * - VENDEDOR: consultar órdenes en modo lectura.
 * - ADMINISTRADOR: consultar y cambiar estados.
 */
@RestController
@RequestMapping(ApiPaths.BOLETAS)
@Tag(name = "Boletas", description = "Gestión de órdenes de compra")
public class BoletaController {

    private final BoletaService boletaService;

    public BoletaController(BoletaService boletaService) {
        this.boletaService = boletaService;
    }

    @PostMapping
    @Operation(summary = "Crea una nueva boleta")
    @ApiResponse(responseCode = "201", description = "Boleta creada")
    @ApiResponse(
            responseCode = "400",
            description = "Datos inválidos o regla de negocio incumplida")
    @ApiResponse(
            responseCode = "404",
            description = "Usuario o producto no encontrado")
    public ResponseEntity<BoletaResponse> crear(

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Datos necesarios para generar una nueva orden de compra",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = CrearBoletaRequest.class),
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

            @Valid
            @RequestBody
            CrearBoletaRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(boletaService.crear(request));
    }

    @GetMapping
    @Operation(
            summary = "Lista las boletas",
            description = "Si se envía usuarioId, devuelve solo las boletas de ese usuario.")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de boletas")
    public List<BoletaResponse> listar(
            @RequestParam(name = "usuarioId", required = false)
            Long usuarioId) {

        if (usuarioId != null) {
            return boletaService.listarPorUsuario(usuarioId);
        }

        return boletaService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene el detalle de una boleta")
    @ApiResponse(
            responseCode = "200",
            description = "Boleta encontrada")
    @ApiResponse(
            responseCode = "404",
            description = "La boleta no existe")
    public BoletaResponse obtener(
            @PathVariable("id") Long id) {

        return boletaService.obtener(id);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambia el estado de una boleta")
    @ApiResponse(
            responseCode = "200",
            description = "Estado actualizado")
    @ApiResponse(
            responseCode = "400",
            description = "Cambio de estado no permitido")
    @ApiResponse(
            responseCode = "404",
            description = "La boleta no existe")
    public BoletaResponse cambiarEstado(
            @PathVariable("id") Long id,
            @Valid @RequestBody CambiarEstadoBoletaRequest request) {

        return boletaService.cambiarEstado(id, request);
    }
}