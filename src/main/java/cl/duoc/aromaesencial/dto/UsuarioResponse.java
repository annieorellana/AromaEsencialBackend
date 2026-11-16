package cl.duoc.aromaesencial.dto;

import java.time.LocalDate;

import cl.duoc.aromaesencial.model.Rol;

public record UsuarioResponse(
        Long id,
        String run,
        String nombre,
        String apellido,
        String correo,
        String telefono,
        LocalDate fechaNacimiento,
        String region,
        String comuna,
        String direccion,
        Rol rol,
        Boolean activo
) {
}