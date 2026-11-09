package cl.duoc.aromaesencial.dto;

import cl.duoc.aromaesencial.model.EstadoBoleta;
import jakarta.validation.constraints.NotNull;

/**
 * Datos utilizados para cambiar el estado de una boleta.
 */
public record CambiarEstadoBoletaRequest(

        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoBoleta estado) {
}