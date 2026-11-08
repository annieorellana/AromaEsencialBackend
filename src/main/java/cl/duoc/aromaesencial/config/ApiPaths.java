package cl.duoc.aromaesencial.config;

/**
 * Rutas base de la API. TODAS las rutas llevan versionado (/api/v1) según las buenas prácticas
 * REST exigidas en la evaluación. Los controladores deben usar estas constantes:
 *
 * <pre>
 * &#64;RequestMapping(ApiPaths.PRODUCTOS)
 * </pre>
 */
public final class ApiPaths {

    public static final String BASE = "/api/v1";

    public static final String AUTH = BASE + "/auth";
    public static final String USUARIOS = BASE + "/usuarios";
    public static final String CATEGORIAS = BASE + "/categorias";
    public static final String PRODUCTOS = BASE + "/productos";
    public static final String BOLETAS = BASE + "/boletas";

    private ApiPaths() {
    }
}
