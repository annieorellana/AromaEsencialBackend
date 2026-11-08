package cl.duoc.aromaesencial.exception;

/**
 * Se lanza cuando se viola una regla de negocio (stock insuficiente, correo duplicado, estado inválido...).
 * El handler global la convierte en HTTP 400 (o 409 si es un conflicto de datos duplicados).
 */
public class ReglaNegocioException extends RuntimeException {

    private final boolean conflicto;

    public ReglaNegocioException(String mensaje) {
        this(mensaje, false);
    }

    /** @param conflicto true para datos duplicados (HTTP 409), false para otras reglas (HTTP 400). */
    public ReglaNegocioException(String mensaje, boolean conflicto) {
        super(mensaje);
        this.conflicto = conflicto;
    }

    public boolean esConflicto() {
        return conflicto;
    }
}
