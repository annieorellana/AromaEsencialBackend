# División del trabajo — Evaluación Parcial 3 (DSY1104, 40%)

Equipo de 4 integrantes. Cada parte incluye **backend + su integración en el frontend + su documento**, de modo que cada persona pueda explicar su parte completa en la presentación individual (60% de la nota) y responder preguntas sobre sus commits en GitHub.

> **Orden de trabajo:** la Parte 1 va primero y se mergea a `develop` el mismo día. Las Partes 2, 3 y 4 parten desde ahí y pueden trabajar **en paralelo** (cada una toca paquetes distintos).

## Resumen

| Parte | Responsable | Backend | Frontend (repo AromaEsencialReact) | Documento |
|---|---|---|---|---|
| **1. Base del proyecto y modelo de datos** | Anastasia | Config, BD, entidades, repositorios, Swagger, CORS, errores, seed | `src/api/httpClient.js` base (URL, headers) | **ERS** + modelo de datos |
| **2. Catálogo (Categoría y Producto)** | Integrante 2 | Servicios + controladores CRUD de productos y categorías | `productoService.js` → API; páginas de tienda y admin de productos | **Documentación de APIs** (Swagger) |
| **3. Boletas / Órdenes** | Integrante 3 | Lógica de compra (stock, totales, cupones, estados) + controladores | `orderService.js` → API; Checkout, admin órdenes, vista vendedor | **Manual de usuario** (pantallazos) |
| **4. Seguridad, usuarios y sesión** | Integrante 4 | Spring Security + JWT, roles, CRUD usuarios | `authService.js` → API, sesión, `RutaProtegida` con roles | **Documento APIs e Integración** |

## Parte 1 — Base del proyecto y modelo de datos (Anastasia) ✅ *lo entregado en este repositorio*

Cubre: **IE3.1.1** (backend con BD, lógica de negocio y modelos) e **IE3.1.2** (describirlo en la presentación).

- [x] Proyecto Spring Boot con todas las dependencias del equipo (JPA, Security, JWT, Swagger, MySQL/H2).
- [x] Conexión a MySQL (variables de entorno) y perfil `h2` para correr sin instalar MySQL.
- [x] Entidades según la Figura 1: `Usuario`, `Boleta`, `DetalleBoleta`, `Producto`, `Categoria` (+ enums `Rol`, `EstadoBoleta`).
- [x] Repositorios con consultas útiles para las otras partes (stock crítico, ofertas, boletas por usuario…).
- [x] Swagger (`/swagger-ui.html`) con esquema de seguridad Bearer listo.
- [x] Rutas versionadas (`/api/v1`) centralizadas en `ApiPaths`.
- [x] CORS para React, manejo global de errores (`ApiError`), `PasswordEncoder` BCrypt.
- [x] `DataSeeder`: 3 categorías, 16 perfumes del frontend y 3 usuarios (admin / vendedor / cliente).
- [x] Endpoint de prueba `GET /api/v1/estado`.
- [ ] Crear repo remoto público en GitHub, rama `develop` e invitar al equipo.
- [ ] Redactar el **ERS** (requerimientos, herramientas, propuesta) usando `docs/MODELO_DATOS.md` como base.
- [ ] Copiar `frontend-catalogo/src/api/httpClient.js` (y `catalogoApi.js`) al frontend en `src/api/`.

**Qué debes saber explicar (presentación IE3.1.2):** por qué esas 5 tablas y sus relaciones, qué hace `ddl-auto=update`, cómo se conecta Spring a MySQL (`application.properties`), por qué la contraseña se guarda con BCrypt, y para qué sirve el seed.

## Parte 2 — Catálogo: Categoría y Producto

Cubre: **IE3.2.1** (API REST + Swagger), parte de **IE3.1.1** (lógica de negocio).

### ✅ Ya adelantado en el repositorio (backend)
- `CategoriaService` y `ProductoService` con las reglas de negocio: código único (se guarda en mayúsculas), precio > 0, stock ≥ 0, categoría obligatoria y existente, no eliminar categoría con productos ni producto ya vendido.
- DTOs con validaciones (`dto/`), `CategoriaController` y `ProductoController` con CRUD completo, `GET /productos/ofertas`, `GET /productos/stock-critico` y filtros por categoría y texto, todo documentado con `@Tag` / `@Operation` / `@ApiResponse`.
- Pruebas: `ProductoServiceTest` y `CategoriaServiceTest` (Mockito) y `CatalogoControllerTest` (MockMvc + H2).
- Frontend: en la carpeta `frontend-catalogo/` están `httpClient.js` y `catalogoApi.js` listos para copiar a `AromaEsencialReact/src/api/`.

### Lo que le toca hacer a la Parte 2
- **Frontend:** copiar los 2 archivos y cambiar `productoService.js` / `useProductos.js` para que usen `catalogoApi.js` en vez de `localStorage` (las funciones ahora son `async`; hay que usar `await` o `useEffect` y manejar estados de carga y error). El formulario de producto del administrador debe elegir la categoría con `obtenerCategorias()` y enviar `categoriaId`.
- Probar todos los endpoints en Swagger y tomar las capturas.
- Si falta una regla o un campo, agregarlo y escribir su prueba.
- Documento: **Documentación de APIs** (endpoints, parámetros, ejemplos JSON, capturas de Swagger). Los ejemplos están en `docs/CONTRATO_API.md`.
- Estudiar el código de `service/` y `controller/` (lo debe poder explicar en la presentación, aunque lo haya adelantado la Parte 1).

## Parte 3 — Boletas / Órdenes

Cubre: **IE3.1.1** (lógica de negocio) e **IE3.2.1** (CRUD de boletas).

- `BoletaService` con `@Transactional`: validar stock, **descontar stock**, calcular subtotal/descuento (cupones `AROMA10` 10 % y `BIENVENIDA15` 15 %, igual que el frontend), costo de envío, total; guardar precio unitario en `DetalleBoleta`.
- Cambio de estado (`EstadoBoleta`): si pasa a `CANCELADA`, **devolver el stock**; impedir cambios inválidos (p. ej. `ENTREGADA` → `ENVIADA`).
- `BoletaController`: crear, listar (admin/vendedor: todas; cliente: solo las suyas), detalle, cambiar estado.
- Pruebas unitarias del cálculo de totales y del descuento/devolución de stock.
- Frontend: `orderService.js` → API; `Checkout.jsx`, `AdminOrdenes`, `AdminOrdenDetalle`, y la **vista del vendedor** (solo productos y órdenes, lectura).
- Documento: **Manual de usuario** con pantallazos detallados (cliente, vendedor, administrador).

## Parte 4 — Seguridad, usuarios y sesión

Cubre: **IE3.3.1**, **IE3.3.2**, **IE3.3.3** (y IE3.3.4–3.3.6 en la presentación).

- Paquete `security/`: `JwtService` (generar, validar, **renovar** token), `JwtAuthenticationFilter`, `UserDetailsService`; reemplazar el `permitAll()` de `SecurityConfig` por reglas por rol.
- `AuthController` (`/api/v1/auth/login`, `/registro`, `/refresh`) y `UsuarioController` (CRUD solo ADMINISTRADOR); validar dominios de correo y RUN como hace el frontend.
- Permisos: ADMINISTRADOR todo; VENDEDOR solo GET productos y GET boletas; CLIENTE solo tienda y sus propias boletas. Usar `@PreAuthorize` y reglas en `SecurityConfig`.
- Frontend: `authService.js` → API; guardar el token y el usuario (`localStorage`/`sessionStorage`) para **persistir la sesión al recargar**; interceptor que agrega `Authorization: Bearer`; cierre de sesión al expirar el token; `RutaProtegida` con roles `ADMINISTRADOR` / `VENDEDOR` / `CLIENTE`; ocultar menús y botones según rol.
- Documento: **Documento APIs e Integración** (flujo de datos frontend ↔ backend, endpoints usados, pruebas y resultados).

## Criterios de la rúbrica y responsable

| Indicador | % | Parte |
|---|---|---|
| IE3.1.1 / IE3.1.2 Backend + BD + lógica + modelos | 8 % / 12 % | 1, 2 y 3 |
| IE3.2.1 / IE3.2.3 API REST + Swagger | 8 % / 12 % | 2 y 3 |
| IE3.2.2 / IE3.2.4 Integración backend ↔ frontend | 6 % / 10 % | 2, 3 y 4 |
| IE3.3.1 / IE3.3.4 Autenticación JWT con roles | 6 % / 10 % | 4 |
| IE3.3.2 / IE3.3.5 Gestión de sesión en frontend | 6 % / 8 % | 4 |
| IE3.3.3 / IE3.3.6 Restricciones de acceso en frontend | 6 % / 8 % | 4 (y 3 para la vista del vendedor) |

> En la ronda de preguntas **cada integrante debe poder explicar las 6 áreas**, no solo la suya: revisen el código de las otras partes antes de presentar.

## Entregables finales (todos juntos)

1. Enlace GitHub público frontend + backend.
2. Proyectos comprimidos (frontend y backend).
3. Documento ERS (Parte 1) · Manual de usuario (Parte 3) · Documentación de APIs (Parte 2) · Documento APIs e Integración (Parte 4).
4. Presentación: 15 min + 5 min de preguntas.

## Flujo de Git (importante: se evalúan los commits)

- Ramas: `main` (estable) ← `develop` ← `feature/parte-2-catalogo`, `feature/parte-3-boletas`, `feature/parte-4-seguridad`.
- Commits pequeños y claros, p. ej. `feat(productos): agrega endpoint de stock crítico`, `fix(boleta): devuelve stock al cancelar`.
- Pull Request a `develop`; al menos una persona revisa antes de mergear.
- No editar `pom.xml`, `SecurityConfig` ni las entidades sin avisar (son los puntos de conflicto). Si una entidad necesita un campo nuevo, pídanselo a la Parte 1.
