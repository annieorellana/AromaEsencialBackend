package cl.duoc.aromaesencial.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos necesarios para crear una nueva boleta.
 */
public record CrearBoletaRequest(

        @NotNull(message = "El usuario es obligatorio")
        Long usuarioId,

        @NotEmpty(message = "La compra debe contener al menos un producto")
        List<@Valid ItemBoletaRequest> items,

        @Size(max = 30, message = "El cupón no puede superar los 30 caracteres")
        String cupon,

        @NotBlank(message = "El método de pago es obligatorio")
        @Size(max = 40, message = "El método de pago no puede superar los 40 caracteres")
        String metodoPago,

        @NotBlank(message = "El método de envío es obligatorio")
        @Size(max = 20, message = "El método de envío no puede superar los 20 caracteres")
        String metodoEnvio,

        @Size(max = 200, message = "La dirección no puede superar los 200 caracteres")
        String direccionEnvio,

        @Size(max = 80, message = "La región no puede superar los 80 caracteres")
        String region,

        @Size(max = 80, message = "La comuna no puede superar los 80 caracteres")
        String comuna,

        @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
        String telefono) {
}