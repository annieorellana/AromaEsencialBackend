# AromaEsencial — Backend (Spring Boot)

Backend REST de la tienda de perfumes **AromaEsencial** · DSY1104 Desarrollo Fullstack II · Evaluación Parcial 3.
Frontend: repositorio `AromaEsencialReact`.

**Stack:** Java 17 · Spring Boot 3.5 · Spring Data JPA · Spring Security + JWT · MySQL · Swagger (springdoc) · Maven.

## Cómo ejecutarlo

> El proyecto incluye Maven Wrapper: **no hace falta instalar Maven**, solo tener JDK 17 o superior.
> En Windows usar `.\mvnw.cmd` en vez de `mvn` (en Linux/Mac, `./mvnw`). En PowerShell, los parámetros `-D...` van entre comillas.

### Opción A — sin instalar nada más (base H2 en memoria)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

### Opción B — con MySQL
1. Tener MySQL corriendo en el puerto 3306 (la base `aromaesencial` se crea sola).
2. Definir las variables (o dejar los valores por defecto de `application.properties`):
   ```bash
   export DB_USER=root
   export DB_PASSWORD=tu_clave
   ```
3. `mvn spring-boot:run`

Al iniciar, el `DataSeeder` carga categorías, 16 perfumes y usuarios de demostración.

| Qué | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Estado de la API | http://localhost:8080/api/v1/estado |
| Consola H2 (solo perfil h2) | http://localhost:8080/h2-console (JDBC: `jdbc:h2:mem:aromaesencial`, usuario `sa`) |

**Usuarios de demostración (solo desarrollo):** `admin@duoc.cl / admin123` · `vendedor@duoc.cl / vendedor123` · `cliente@duoc.cl / cliente123`

### Pruebas
```bash
mvn test
```

## Estructura

```
src/main/java/cl/duoc/aromaesencial
├── AromaEsencialApplication.java
├── config/        ApiPaths, CorsConfig, OpenApiConfig, SecurityConfig, DataSeeder
├── controller/    EstadoController, CategoriaController, ProductoController (+ Partes 3 y 4)
├── exception/     ApiError, GlobalExceptionHandler, RecursoNoEncontradoException, ReglaNegocioException
├── model/         Usuario, Boleta, DetalleBoleta, Producto, Categoria, Rol, EstadoBoleta
├── repository/    un JpaRepository por entidad
├── service/       CategoriaService, ProductoService (+ Partes 3 y 4)
├── dto/           Request/Response de categorías y productos (+ Partes 3 y 4)  ← los controladores NO exponen entidades
└── security/      (Parte 4)  JwtService, filtro JWT, UserDetailsService

frontend-catalogo/src/api/   httpClient.js y catalogoApi.js para copiar al proyecto React
```

## Convenciones del equipo

- Rutas siempre con `ApiPaths` (`/api/v1/...`). Controladores con `@Tag` y `@Operation` para Swagger.
- Servicios lanzan `RecursoNoEncontradoException` (404) o `ReglaNegocioException` (400/409); el handler global arma la respuesta.
- Los controladores trabajan con **DTOs**, nunca con las entidades (sobre todo `Usuario`, por la contraseña).
- Detalles de la división en `docs/DIVISION_EQUIPO.md`, esquema de BD en `docs/MODELO_DATOS.md` y endpoints acordados en `docs/CONTRATO_API.md`.

## Conectar el frontend React (resumen)

Copiar la carpeta `frontend-catalogo/src/api/` de este repo a `AromaEsencialReact/src/api/` (trae `httpClient.js` y `catalogoApi.js`). Versión resumida de `httpClient.js`:

```js
export const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api/v1';

export async function http(ruta, { metodo = 'GET', cuerpo, token } = {}) {
  const respuesta = await fetch(`${API_URL}${ruta}`, {
    method: metodo,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: cuerpo ? JSON.stringify(cuerpo) : undefined,
  });
  const datos = await respuesta.json().catch(() => null);
  if (!respuesta.ok) throw new Error(datos?.mensaje || 'Error al comunicarse con el servidor');
  return datos;
}
```

Primera prueba de integración: `http('/estado')` debe devolver `{ estado: "OK", ... }` (si falla por CORS, revisar `app.cors.allowed-origins`).
