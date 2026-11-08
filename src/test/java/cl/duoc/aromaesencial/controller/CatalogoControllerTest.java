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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prueba de integración de los endpoints del catálogo contra la base H2 con los datos del seed.
 * Es @Transactional: cada prueba se deshace al terminar y no afecta a las demás.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CatalogoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CategoriaRepository categoriaRepository;

    private Long idCategoria(String nombre) {
        return categoriaRepository.findByNombreIgnoreCase(nombre).orElseThrow().getId();
    }

    private String productoJson(String codigo, String nombre, int precio, Long categoriaId) {
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
                """.formatted(codigo, nombre, precio, categoriaId);
    }

    @Test
    void listaTodosLosProductosDelCatalogo() throws Exception {
        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(16));
    }

    @Test
    void filtraProductosPorCategoria() throws Exception {
        mockMvc.perform(get("/api/v1/productos").param("categoriaId", idCategoria("Hombre").toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoriaNombre").value("Hombre"));
    }

    @Test
    void listaProductosConStockCritico() throws Exception {
        mockMvc.perform(get("/api/v1/productos/stock-critico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].enStockCritico").value(true));
    }

    @Test
    void creaUnProducto() throws Exception {
        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson("pf-900", "Aroma de Prueba", 45000, idCategoria("Mujer"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.codigo").value("PF-900"))
                .andExpect(jsonPath("$.categoriaNombre").value("Mujer"));
    }

    @Test
    void rechazaUnCodigoDuplicadoConConflicto() throws Exception {
        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson("PF-016", "Repetido", 45000, idCategoria("Hombre"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void rechazaDatosInvalidosConDetalle() throws Exception {
        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson("PF-901", "", 0, idCategoria("Hombre"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles").isNotEmpty());
    }

    @Test
    void devuelve404SiElProductoNoExiste() throws Exception {
        mockMvc.perform(get("/api/v1/productos/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void noEliminaUnaCategoriaQueTieneProductos() throws Exception {
        mockMvc.perform(delete("/api/v1/categorias/" + idCategoria("Hombre")))
                .andExpect(status().isConflict());
    }

    @Test
    void listaLasCategorias() throws Exception {
        mockMvc.perform(get("/api/v1/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }
}
