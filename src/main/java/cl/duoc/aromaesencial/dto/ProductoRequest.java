package cl.duoc.aromaesencial.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos que envía el cliente para crear o actualizar un producto. */
public record ProductoRequest(

        @NotBlank(message = "El código es obligatorio")
        @Size(max = 20, message = "El código no puede superar los 20 caracteres")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String nombre,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String descripcion,

        @NotNull(message = "El precio es obligatorio")
        @Min(value = 1, message = "El precio debe ser mayor a 0")
        Integer precio,

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,

        @NotNull(message = "El stock crítico es obligatorio")
        @Min(value = 0, message = "El stock crítico no puede ser negativo")
        Integer stockCritico,

        @Size(max = 500, message = "La URL de la imagen no puede superar los 500 caracteres")
        String imagen,

        /** Si no se envía se considera false. */
        Boolean oferta,

        @NotNull(message = "La categoría es obligatoria")
        Long categoriaId) {
}
