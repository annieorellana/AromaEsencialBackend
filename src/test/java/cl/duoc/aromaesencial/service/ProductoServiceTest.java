package cl.duoc.aromaesencial.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import cl.duoc.aromaesencial.dto.ProductoRequest;
import cl.duoc.aromaesencial.dto.ProductoResponse;
import cl.duoc.aromaesencial.exception.RecursoNoEncontradoException;
import cl.duoc.aromaesencial.exception.ReglaNegocioException;
import cl.duoc.aromaesencial.model.Categoria;
import cl.duoc.aromaesencial.model.Producto;
import cl.duoc.aromaesencial.repository.CategoriaRepository;
import cl.duoc.aromaesencial.repository.DetalleBoletaRepository;
import cl.duoc.aromaesencial.repository.ProductoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private DetalleBoletaRepository detalleBoletaRepository;

    @InjectMocks
    private ProductoService productoService;

    private Categoria hombre;

    @BeforeEach
    void preparar() {
        hombre = Categoria.builder().id(1L).nombre("Hombre").build();
    }

    private ProductoRequest request(String codigo) {
        return new ProductoRequest(codigo, "  Nuevo Aroma ", "Descripción", 50000, 10, 3, null, null, 1L);
    }

    @Test
    void crearNormalizaElCodigoYGuardaElProducto() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(hombre));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            p.setId(99L);
            return p;
        });

        ProductoResponse respuesta = productoService.crear(request(" pf-100 "));

        assertEquals(99L, respuesta.id());
        assertEquals("PF-100", respuesta.codigo());
        assertEquals("Nuevo Aroma", respuesta.nombre());
        assertFalse(respuesta.oferta(), "Sin dato de oferta debe quedar en false");
        assertEquals("Hombre", respuesta.categoriaNombre());
    }

    @Test
    void crearConCodigoDuplicadoLanzaConflicto() {
        when(productoRepository.existsByCodigo("PF-100")).thenReturn(true);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> productoService.crear(request("PF-100")));

        assertTrue(ex.esConflicto());
        verify(productoRepository, never()).save(any());
    }

    @Test
    void crearConCategoriaInexistenteLanzaNoEncontrado() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> productoService.crear(request("PF-101")));
        verify(productoRepository, never()).save(any());
    }

    @Test
    void obtenerProductoInexistenteLanzaNoEncontrado() {
        when(productoRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> productoService.obtener(7L));
    }

    @Test
    void marcaStockCriticoCuandoStockEsMenorOIgualAlCritico() {
        Producto p = Producto.builder().id(5L).codigo("PF-005").nombre("Bajo").precio(1000)
                .stock(3).stockCritico(3).oferta(false).categoria(hombre).build();
        when(productoRepository.findById(5L)).thenReturn(Optional.of(p));

        assertTrue(productoService.obtener(5L).enStockCritico());
    }

    @Test
    void noEliminaUnProductoQueYaFueVendido() {
        Producto p = Producto.builder().id(5L).codigo("PF-005").nombre("Vendido").precio(1000)
                .stock(10).stockCritico(3).categoria(hombre).build();
        when(productoRepository.findById(5L)).thenReturn(Optional.of(p));
        when(detalleBoletaRepository.existsByProductoId(5L)).thenReturn(true);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> productoService.eliminar(5L));

        assertTrue(ex.esConflicto());
        verify(productoRepository, never()).delete(any());
    }

    @Test
    void eliminaUnProductoSinVentas() {
        Producto p = Producto.builder().id(5L).codigo("PF-005").nombre("Libre").precio(1000)
                .stock(10).stockCritico(3).categoria(hombre).build();
        when(productoRepository.findById(5L)).thenReturn(Optional.of(p));
        when(detalleBoletaRepository.existsByProductoId(5L)).thenReturn(false);

        productoService.eliminar(5L);

        verify(productoRepository).delete(p);
    }
}
