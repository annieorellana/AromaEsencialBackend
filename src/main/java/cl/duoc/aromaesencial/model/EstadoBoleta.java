package cl.duoc.aromaesencial.model;

/**
 * Estados de una boleta (orden). Equivalen a los estados que ya usa el frontend
 * ('Confirmada', 'En preparación', 'Enviada', 'Entregada', 'Cancelada').
 */
public enum EstadoBoleta {
    CONFIRMADA("Confirmada"),
    EN_PREPARACION("En preparación"),
    ENVIADA("Enviada"),
    ENTREGADA("Entregada"),
    CANCELADA("Cancelada");

    private final String etiqueta;

    EstadoBoleta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
