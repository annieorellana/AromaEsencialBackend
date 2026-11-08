package cl.duoc.aromaesencial.controller;

import java.time.LocalDateTime;
import java.util.Map;

import cl.duoc.aromaesencial.config.ApiPaths;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de verificación: sirve para comprobar que el backend está arriba y que el frontend
 * logra comunicarse con él (primera prueba de integración REST + CORS).
 */
@RestController
@RequestMapping(ApiPaths.BASE + "/estado")
@Tag(name = "Estado", description = "Verificación del servicio")
public class EstadoController {

    @GetMapping
    @Operation(summary = "Comprueba que la API está disponible")
    public Map<String, Object> estado() {
        return Map.of(
                "servicio", "AromaEsencial API",
                "version", "v1",
                "estado", "OK",
                "fecha", LocalDateTime.now().toString());
    }
}
