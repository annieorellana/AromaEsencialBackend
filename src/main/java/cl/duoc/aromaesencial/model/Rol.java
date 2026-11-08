package cl.duoc.aromaesencial.model;

/**
 * Roles del sistema (según el enunciado):
 * <ul>
 *   <li>ADMINISTRADOR: acceso total.</li>
 *   <li>VENDEDOR: solo ve lista/detalle de productos y de órdenes (boletas).</li>
 *   <li>CLIENTE: solo accede a la tienda.</li>
 * </ul>
 * En Spring Security la autoridad es "ROLE_" + nombre (p. ej. ROLE_ADMINISTRADOR).
 */
public enum Rol {
    ADMINISTRADOR,
    VENDEDOR,
    CLIENTE
}
