// Llamadas a la API del catálogo (productos y categorías).
// Copiar a: AromaEsencialReact/src/api/catalogoApi.js
//
// Estas funciones devuelven los productos con la MISMA forma que usa hoy el frontend
// (id, codigo, nombre, categoria, precio, stock, stockCritico, descripcion, imagen, oferta),
// así las páginas casi no necesitan cambios. Son async: hay que usar await / .then().
import { http } from './httpClient';

// ---------- Conversión API <-> formato del frontend ----------

// De la respuesta de la API al objeto que usan las páginas.
// El id se convierte a texto porque las rutas del frontend (/productos/:id) lo entregan como texto.
export function aProductoFront(p) {
  return {
    id: String(p.id),
    codigo: p.codigo,
    nombre: p.nombre,
    categoria: p.categoriaNombre,
    categoriaId: p.categoriaId,
    precio: p.precio,
    stock: p.stock,
    stockCritico: p.stockCritico,
    enStockCritico: p.enStockCritico,
    descripcion: p.descripcion,
    imagen: p.imagen,
    oferta: p.oferta,
  };
}

// Del objeto del formulario del frontend al cuerpo que espera la API.
// Requiere `categoriaId` (el formulario de admin debe elegir la categoría desde obtenerCategorias()).
export function aProductoRequest(p) {
  return {
    codigo: p.codigo,
    nombre: p.nombre,
    descripcion: p.descripcion,
    precio: Number(p.precio),
    stock: Number(p.stock),
    stockCritico: Number(p.stockCritico),
    imagen: p.imagen,
    oferta: Boolean(p.oferta),
    categoriaId: Number(p.categoriaId),
  };
}

// ---------- Productos ----------

export async function obtenerProductos({ categoriaId, buscar } = {}) {
  const parametros = new URLSearchParams();
  if (categoriaId) parametros.set('categoriaId', categoriaId);
  if (buscar) parametros.set('buscar', buscar);
  const consulta = parametros.toString() ? `?${parametros}` : '';

  const lista = await http(`/productos${consulta}`);
  return lista.map(aProductoFront);
}

export async function obtenerProductoPorId(id) {
  return aProductoFront(await http(`/productos/${id}`));
}

export async function obtenerProductosEnOferta() {
  return (await http('/productos/ofertas')).map(aProductoFront);
}

export async function obtenerProductosStockCritico(token) {
  return (await http('/productos/stock-critico', { token })).map(aProductoFront);
}

export async function crearProducto(producto, token) {
  return aProductoFront(await http('/productos', { metodo: 'POST', cuerpo: aProductoRequest(producto), token }));
}

export async function actualizarProducto(id, producto, token) {
  return aProductoFront(
    await http(`/productos/${id}`, { metodo: 'PUT', cuerpo: aProductoRequest(producto), token })
  );
}

export async function eliminarProducto(id, token) {
  await http(`/productos/${id}`, { metodo: 'DELETE', token });
  return true;
}

// ---------- Categorías ----------

export function obtenerCategorias() {
  return http('/categorias'); // [{ id, nombre, descripcion }]
}

export function crearCategoria(categoria, token) {
  return http('/categorias', { metodo: 'POST', cuerpo: categoria, token });
}

export function actualizarCategoria(id, categoria, token) {
  return http(`/categorias/${id}`, { metodo: 'PUT', cuerpo: categoria, token });
}

export async function eliminarCategoria(id, token) {
  await http(`/categorias/${id}`, { metodo: 'DELETE', token });
  return true;
}
