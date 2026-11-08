# Contrato de la API (acuerdo entre las 4 partes)

Base: `http://localhost:8080/api/v1` · Formato: JSON · Errores: siempre `ApiError`
(`timestamp`, `status`, `error`, `mensaje`, `ruta`, `detalles[]`).

Las rutas están definidas en `config/ApiPaths.java`. Este contrato permite que el frontend avance sin esperar al backend de otra parte.

## Autenticación — Parte 4
| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/auth/login` | público | Entrega `{ token, tipo: "Bearer", usuario: {id, nombre, correo, rol} }` |
| POST | `/auth/registro` | público | Crea un CLIENTE |
| POST | `/auth/refresh` | autenticado | Renueva el token |
| GET/POST/PUT/DELETE | `/usuarios`, `/usuarios/{id}` | ADMINISTRADOR | CRUD de usuarios |

El token se envía en cada petición: `Authorization: Bearer <token>`.

## Categorías — Parte 2
| Método | Ruta | Acceso |
|---|---|---|
| GET | `/categorias`, `/categorias/{id}` | público |
| POST / PUT / DELETE | `/categorias`, `/categorias/{id}` | ADMINISTRADOR |

## Productos — Parte 2
| Método | Ruta | Acceso |
|---|---|---|
| GET | `/productos` (filtros opcionales `?categoriaId=`, `?buscar=`) | público (VENDEDOR también) |
| GET | `/productos/{id}`, `/productos/ofertas` | público |
| GET | `/productos/stock-critico` | ADMINISTRADOR |
| POST / PUT / DELETE | `/productos`, `/productos/{id}` | ADMINISTRADOR |

### Ejemplos del catálogo (ya implementado)

`POST /api/v1/productos` — cuerpo:
```json
{
  "codigo": "PF-020",
  "nombre": "Ocean Breeze",
  "descripcion": "Notas marinas y cítricas.",
  "precio": 59000,
  "stock": 15,
  "stockCritico": 5,
  "imagen": "https://ejemplo.cl/ocean.jpg",
  "oferta": false,
  "categoriaId": 1
}
```
Respuesta `201 Created`:
```json
{
  "id": 17, "codigo": "PF-020", "nombre": "Ocean Breeze", "descripcion": "Notas marinas y cítricas.",
  "precio": 59000, "stock": 15, "stockCritico": 5, "enStockCritico": false,
  "imagen": "https://ejemplo.cl/ocean.jpg", "oferta": false,
  "categoriaId": 1, "categoriaNombre": "Hombre"
}
```
`POST /api/v1/categorias` — cuerpo: `{ "nombre": "Infantil", "descripcion": "Fragancias suaves" }`

Errores (siempre el mismo formato):
```json
{
  "timestamp": "2026-11-08T12:40:00", "status": 400, "error": "Bad Request",
  "mensaje": "Los datos enviados no son válidos.", "ruta": "/api/v1/productos",
  "detalles": ["precio: El precio debe ser mayor a 0"]
}
```
| Situación | Código |
|---|---|
| Datos inválidos | 400 |
| Producto / categoría no existe | 404 |
| Código o nombre duplicado; eliminar categoría con productos o producto ya vendido | 409 |

## Boletas — Parte 3
| Método | Ruta | Acceso |
|---|---|---|
| POST | `/boletas` | CLIENTE (y ADMINISTRADOR) |
| GET | `/boletas` | ADMINISTRADOR y VENDEDOR: todas · CLIENTE: solo las suyas |
| GET | `/boletas/{id}` | igual que arriba |
| PATCH | `/boletas/{id}/estado` | ADMINISTRADOR |

## Verificación — Parte 1 (ya implementado)
| Método | Ruta | Acceso |
|---|---|---|
| GET | `/estado` | público |

## Matriz de permisos por rol

| Recurso | ADMINISTRADOR | VENDEDOR | CLIENTE |
|---|:-:|:-:|:-:|
| Tienda (ver productos, comprar) | ✔ | ✔ | ✔ |
| Productos: listar y ver detalle (panel) | ✔ | ✔ | — |
| Productos: crear / editar / eliminar | ✔ | — | — |
| Órdenes: listar y ver detalle | ✔ | ✔ | solo las propias |
| Órdenes: cambiar estado | ✔ | — | — |
| Usuarios, reportes, stock crítico | ✔ | — | — |

> Cualquier cambio en este contrato se avisa al grupo y se actualiza este archivo en el mismo commit.
