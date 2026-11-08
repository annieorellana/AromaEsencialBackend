package cl.duoc.aromaesencial.exception;

import java.time.LocalDateTime;
import java.util.List;

/** Formato único de error de la API. El frontend siempre recibe esta estructura. */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        String ruta,
        List<String> detalles) {
}
