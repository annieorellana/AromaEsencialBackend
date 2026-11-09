package cl.duoc.aromaesencial.dto;

import java.time.LocalDateTime;
import java.util.List;

import cl.duoc.aromaesencial.model.Boleta;
import cl.duoc.aromaesencial.model.EstadoBoleta;

/**
 * Datos de una boleta que serán enviados al frontend.
 */
public record BoletaResponse(
        Long id,
        Long usuarioId,
        String nombreCliente,
        String correoCliente,
        LocalDateTime fecha,
        EstadoBoleta estado,
        Integer subtotal,
        Integer descuento,
        Integer costoEnvio,
        Integer total,
        String cupon,
        String metodoPago,
        String metodoEnvio,
        String direccionEnvio,
        String region,
        String comuna,
        String telefono,
        List<DetalleBoletaResponse> detalles) {

    public static BoletaResponse from(Boleta boleta) {

        List<DetalleBoletaResponse> detalles = boleta.getDetalles()
                .stream()
                .map(DetalleBoletaResponse::from)
                .toList();

        return new BoletaResponse(
                boleta.getId(),
                boleta.getUsuario() != null ? boleta.getUsuario().getId() : null,
                boleta.getUsuario() != null
                        ? boleta.getUsuario().getNombre() + " " + boleta.getUsuario().getApellido()
                        : null,
                boleta.getUsuario() != null ? boleta.getUsuario().getCorreo() : null,
                boleta.getFecha(),
                boleta.getEstado(),
                boleta.getSubtotal(),
                boleta.getDescuento(),
                boleta.getCostoEnvio(),
                boleta.getTotal(),
                boleta.getCupon(),
                boleta.getMetodoPago(),
                boleta.getMetodoEnvio(),
                boleta.getDireccionEnvio(),
                boleta.getRegion(),
                boleta.getComuna(),
                boleta.getTelefono(),
                detalles);
    }
}