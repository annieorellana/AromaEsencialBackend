package cl.duoc.aromaesencial.dto;

import cl.duoc.aromaesencial.model.Producto;

/**
 * Producto tal como se devuelve al cliente.
 * {@code enStockCritico} es true cuando stock <= stockCritico (el frontend lo usa para alertar al administrador).
 */
public record ProductoResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        Integer precio,
        Integer stock,
        Integer stockCritico,
        boolean enStockCritico,
        String imagen,
        boolean oferta,
        Long categoriaId,
        String categoriaNombre) {

    /** Debe llamarse dentro de una transacción (la categoría se carga de forma perezosa). */
    public static ProductoResponse from(Producto p) {
        return new ProductoResponse(
                p.getId(),
                p.getCodigo(),
                p.getNombre(),
                p.getDescripcion(),
                p.getPrecio(),
                p.getStock(),
                p.getStockCritico(),
                p.getStock() <= p.getStockCritico(),
                p.getImagen(),
                Boolean.TRUE.equals(p.getOferta()),
                p.getCategoria().getId(),
                p.getCategoria().getNombre());
    }
}
