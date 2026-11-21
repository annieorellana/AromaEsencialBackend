
package cl.duoc.aromaesencial.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.aromaesencial.repository.CategoriaRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pruebas de integración de los endpoints del catálogo.
 *
 * Se utiliza la base de datos H2 con los datos iniciales.
 *
 * @Transactional permite revertir los cambios
 * realizados al finalizar cada prueba.
 *
 * Las operaciones administrativas utilizan
 * @WithMockUser para simular un administrador.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CatalogoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // ==========================================
    // OBTENER ID DE UNA CATEGORÍA
    // ==========================================

    private Long idCategoria(String nombre) {

        return categoriaRepository
                .findByNombreIgnoreCase(nombre)
                .orElseThrow()
                .getId();
    }

    // ==========================================
    // CREAR JSON DE PRODUCTO PARA PRUEBAS
    // ==========================================

    private String productoJson(
            String codigo,
            String nombre,
            int precio,
            Long categoriaId) {

        return """
                {
                  "codigo": "%s",
                  "nombre": "%s",
                  "descripcion": "Producto de prueba",
                  "precio": %d,
                  "stock": 10,
                  "stockCritico": 2,
                  "imagen": "https://ejemplo.cl/img.jpg",
                  "oferta": false,
                  "categoriaId": %d
                }
                """.formatted(
                    codigo,
                    nombre,
                    precio,
                    categoriaId
                );
    }

    // ==========================================
    // PRUEBA 1: LISTAR PRODUCTOS
    // ACCESO PÚBLICO
    // ==========================================

    @Test
    void listaTodosLosProductosDelCatalogo()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/productos")
        )
                .andExpect(status().isOk())
                .andExpect(
                    jsonPath("$.length()").value(16)
                );
    }

    // ==========================================
    // PRUEBA 2: FILTRAR POR CATEGORÍA
    // ACCESO PÚBLICO
    // ==========================================

    @Test
    void filtraProductosPorCategoria()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/productos")
                    .param(
                        "categoriaId",
                        idCategoria("Hombre").toString()
                    )
        )
                .andExpect(status().isOk())
                .andExpect(
                    jsonPath("$[0].categoriaNombre")
                        .value("Hombre")
                );
    }

    // ==========================================
    // PRUEBA 3: LISTAR STOCK CRÍTICO
    // REQUIERE ADMINISTRADOR
    // ==========================================

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void listaProductosConStockCritico()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/productos/stock-critico")
        )
                .andExpect(status().isOk())
                .andExpect(
                    jsonPath("$[0].enStockCritico")
                        .value(true)
                );
    }

    // ==========================================
    // PRUEBA 4: CREAR PRODUCTO
    // REQUIERE ADMINISTRADOR
    // ==========================================

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void creaUnProducto()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/productos")
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content(
                        productoJson(
                            "pf-900",
                            "Aroma de Prueba",
                            45000,
                            idCategoria("Mujer")
                        )
                    )
        )
                .andExpect(status().isCreated())
                .andExpect(
                    jsonPath("$.id").exists()
                )
                .andExpect(
                    jsonPath("$.codigo")
                        .value("PF-900")
                )
                .andExpect(
                    jsonPath("$.categoriaNombre")
                        .value("Mujer")
                );
    }

    // ==========================================
    // PRUEBA 5: RECHAZAR CÓDIGO DUPLICADO
    // REQUIERE ADMINISTRADOR
    // ==========================================

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void rechazaUnCodigoDuplicadoConConflicto()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/productos")
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content(
                        productoJson(
                            "PF-016",
                            "Repetido",
                            45000,
                            idCategoria("Hombre")
                        )
                    )
        )
                .andExpect(status().isConflict())
                .andExpect(
                    jsonPath("$.status")
                        .value(409)
                );
    }

    // ==========================================
    // PRUEBA 6: RECHAZAR DATOS INVÁLIDOS
    // REQUIERE ADMINISTRADOR
    // ==========================================

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void rechazaDatosInvalidosConDetalle()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/productos")
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content(
                        productoJson(
                            "PF-901",
                            "",
                            0,
                            idCategoria("Hombre")
                        )
                    )
        )
                .andExpect(status().isBadRequest())
                .andExpect(
                    jsonPath("$.detalles")
                        .isNotEmpty()
                );
    }

    // ==========================================
    // PRUEBA 7: PRODUCTO INEXISTENTE
    // ACCESO PÚBLICO
    // ==========================================

    @Test
    void devuelve404SiElProductoNoExiste()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/productos/999999")
        )
                .andExpect(status().isNotFound())
                .andExpect(
                    jsonPath("$.mensaje").exists()
                );
    }

    // ==========================================
    // PRUEBA 8: NO ELIMINAR CATEGORÍA
    // CON PRODUCTOS ASOCIADOS
    // REQUIERE ADMINISTRADOR
    // ==========================================

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void noEliminaUnaCategoriaQueTieneProductos()
            throws Exception {

        mockMvc.perform(
                delete(
                    "/api/v1/categorias/"
                    + idCategoria("Hombre")
                )
        )
                .andExpect(status().isConflict());
    }

    // ==========================================
    // PRUEBA 9: LISTAR CATEGORÍAS
    // ACCESO PÚBLICO
    // ==========================================

    @Test
    void listaLasCategorias()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/categorias")
        )
                .andExpect(status().isOk())
                .andExpect(
                    jsonPath("$.length()").value(3)
                );
    }
}
