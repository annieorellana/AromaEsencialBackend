package cl.duoc.aromaesencial.exception;

/** Se lanza cuando un registro no existe. El handler global la convierte en HTTP 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public RecursoNoEncontradoException(String recurso, Object id) {
        super(recurso + " no encontrado con id: " + id);
    }
}
