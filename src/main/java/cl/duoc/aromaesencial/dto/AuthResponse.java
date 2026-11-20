
package cl.duoc.aromaesencial.dto;

public record AuthResponse(

    String token,

    String tipo,

    Long id,

    String nombre,

    String correo,

    String rol

) {
}
