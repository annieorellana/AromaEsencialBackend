package cl.duoc.aromaesencial.dto;

import cl.duoc.aromaesencial.model.DetalleBoleta;

/**
 * Datos de cada producto incluido en una boleta.
 */
public record DetalleBoletaResponse(
        Long id,
        Long productoId,
        String codigoProducto,
        String nombreProducto,
        Integer cantidad,
        Integer precioUnitario,
        Integer subtotal) {

    public static DetalleBoletaResponse from(DetalleBoleta detalle) {
        return new DetalleBoletaResponse(
                detalle.getId(),
                detalle.getProducto().getId(),
                detalle.getProducto().getCodigo(),
                detalle.getProducto().getNombre(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal());
    }
}