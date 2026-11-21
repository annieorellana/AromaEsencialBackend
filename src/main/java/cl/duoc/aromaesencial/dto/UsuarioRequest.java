package cl.duoc.aromaesencial.dto;

import java.time.LocalDate;

import cl.duoc.aromaesencial.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(

        @NotBlank(message = "El RUN es obligatorio")
        @Size(max = 12)
        String run,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60)
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 60)
        String apellido,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es valido")
        @Size(max = 120)
        String correo,

        String password,

        @Size(max = 20)
        String telefono,

        LocalDate fechaNacimiento,

        @Size(max = 80)
        String region,

        @Size(max = 80)
        String comuna,

        @Size(max = 200)
        String direccion,

        Rol rol,

        Boolean activo
) {
}