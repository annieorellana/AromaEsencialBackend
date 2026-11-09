package cl.duoc.aromaesencial.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Representa un producto y su cantidad dentro de una compra.
 */
public record ItemBoletaRequest(

        @NotNull(message = "El producto es obligatorio")
        Long productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser mayor a 0")
        Integer cantidad) {
}