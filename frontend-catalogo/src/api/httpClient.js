// Cliente HTTP base para hablar con el backend Spring Boot.
// Copiar a: AromaEsencialReact/src/api/httpClient.js
//
// URL de la API: se puede cambiar creando un archivo .env en el frontend con
//   REACT_APP_API_URL=http://localhost:8080/api/v1
export const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api/v1';

/**
 * Hace una petición a la API y devuelve el JSON de la respuesta.
 * - Si el backend responde con error (400, 404, 409...), lanza un Error con el mensaje de la API.
 * - `token` (opcional): token JWT; lo agrega la Parte 4 cuando exista el login.
 */
export async function http(ruta, { metodo = 'GET', cuerpo, token } = {}) {
  let respuesta;

  try {
    respuesta = await fetch(`${API_URL}${ruta}`, {
      method: metodo,
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: cuerpo !== undefined ? JSON.stringify(cuerpo) : undefined,
    });
  } catch (error) {
    throw new Error('No se pudo conectar con el servidor. ¿Está encendido el backend?');
  }

  // 204 No Content (por ejemplo al eliminar): no hay cuerpo que leer
  if (respuesta.status === 204) {
    return null;
  }

  const datos = await respuesta.json().catch(() => null);

  if (!respuesta.ok) {
    const detalle = datos?.detalles?.length ? ` (${datos.detalles.join('; ')})` : '';
    throw new Error((datos?.mensaje || 'Error al comunicarse con el servidor') + detalle);
  }

  return datos;
}
