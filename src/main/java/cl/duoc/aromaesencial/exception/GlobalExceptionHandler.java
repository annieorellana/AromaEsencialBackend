
package cl.duoc.aromaesencial.exception;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Manejo global de excepciones de AromaEsencial.
 *
 * Convierte los errores del backend en respuestas JSON
 * uniformes utilizando ApiError.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ==========================================
    // RECURSO NO ENCONTRADO - 404
    // ==========================================

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> noEncontrado(
            RecursoNoEncontradoException ex,
            HttpServletRequest req) {

        return construir(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                req,
                List.of()
        );
    }

    // ==========================================
    // REGLAS DE NEGOCIO - 400 O 409
    // ==========================================

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiError> reglaNegocio(
            ReglaNegocioException ex,
            HttpServletRequest req) {

        HttpStatus estado = ex.esConflicto()
                ? HttpStatus.CONFLICT
                : HttpStatus.BAD_REQUEST;

        return construir(
                estado,
                ex.getMessage(),
                req,
                List.of()
        );
    }

    // ==========================================
    // ERRORES HTTP DEFINIDOS POR LOS SERVICIOS
    // ==========================================

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> respuestaEstado(
            ResponseStatusException ex,
            HttpServletRequest req) {

        HttpStatusCode estado = ex.getStatusCode();

        String mensaje = ex.getReason() != null
                ? ex.getReason()
                : "No se pudo completar la operación.";

        return construir(
                estado,
                mensaje,
                req,
                List.of()
        );
    }

    // ==========================================
    // VALIDACION DE CAMPOS - 400
    // ==========================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(
            MethodArgumentNotValidException ex,
            HttpServletRequest req) {

        List<String> detalles = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": "
                        + e.getDefaultMessage())
                .toList();

        return construir(
                HttpStatus.BAD_REQUEST,
                "Los datos enviados no son válidos.",
                req,
                detalles
        );
    }

    // ==========================================
    // PETICIONES MAL FORMADAS - 400
    // ==========================================

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiError> peticionMalFormada(
            Exception ex,
            HttpServletRequest req) {

        return construir(
                HttpStatus.BAD_REQUEST,
                "La petición está mal formada o tiene un tipo de dato incorrecto.",
                req,
                List.of()
        );
    }

    // ==========================================
    // RESTRICCIONES DE BASE DE DATOS - 409
    // ==========================================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integridad(
            DataIntegrityViolationException ex,
            HttpServletRequest req) {

        return construir(
                HttpStatus.CONFLICT,
                "La operación viola una restricción de la base de datos.",
                req,
                List.of()
        );
    }

    // ==========================================
    // ACCESO DENEGADO - 403
    // ==========================================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> accesoDenegado(
            AccessDeniedException ex,
            HttpServletRequest req) {

        return construir(
                HttpStatus.FORBIDDEN,
                "No tienes permisos para realizar esta acción.",
                req,
                List.of()
        );
    }

    // ==========================================
    // ERRORES INESPERADOS - 500
    // ==========================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inesperado(
            Exception ex,
            HttpServletRequest req) {

        log.error(
                "Error inesperado en {}",
                req.getRequestURI(),
                ex
        );

        return construir(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno en el servidor.",
                req,
                List.of()
        );
    }

    // ==========================================
    // CONSTRUCCION DE RESPUESTAS JSON
    // ==========================================

    private ResponseEntity<ApiError> construir(
            HttpStatusCode estado,
            String mensaje,
            HttpServletRequest req,
            List<String> detalles) {

        String descripcion = estado instanceof HttpStatus
                ? ((HttpStatus) estado).getReasonPhrase()
                : "HTTP " + estado.value();

        ApiError cuerpo = new ApiError(
                LocalDateTime.now(),
                estado.value(),
                descripcion,
                mensaje,
                req.getRequestURI(),
                detalles
        );

        return ResponseEntity.status(estado).body(cuerpo);
    }
}
