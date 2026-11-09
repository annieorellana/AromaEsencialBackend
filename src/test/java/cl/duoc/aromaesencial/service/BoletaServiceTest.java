package cl.duoc.aromaesencial.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import cl.duoc.aromaesencial.dto.BoletaResponse;
import cl.duoc.aromaesencial.dto.CambiarEstadoBoletaRequest;
import cl.duoc.aromaesencial.dto.CrearBoletaRequest;
import cl.duoc.aromaesencial.dto.ItemBoletaRequest;
import cl.duoc.aromaesencial.exception.ReglaNegocioException;
import cl.duoc.aromaesencial.model.Boleta;
import cl.duoc.aromaesencial.model.EstadoBoleta;
import cl.duoc.aromaesencial.model.Producto;
import cl.duoc.aromaesencial.model.Usuario;
import cl.duoc.aromaesencial.repository.BoletaRepository;
import cl.duoc.aromaesencial.repository.ProductoRepository;
import cl.duoc.aromaesencial.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BoletaServiceTest {

    @Mock
    private BoletaRepository boletaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private BoletaService boletaService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {

        boletaService = new BoletaService(
                boletaRepository,
                productoRepository,
                usuarioRepository);

        usuario = mock(Usuario.class);

        when(usuario.getId()).thenReturn(3L);
        when(usuario.getNombre()).thenReturn("Cliente");
        when(usuario.getApellido()).thenReturn("Demo");
        when(usuario.getCorreo()).thenReturn("cliente@duoc.cl");

        when(usuarioRepository.findById(3L))
                .thenReturn(Optional.of(usuario));

        /*
         * Cuando el servicio guarde una boleta,
         * Mockito devuelve la misma boleta recibida.
         */
        when(boletaRepository.save(any(Boleta.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    // =========================================================
    // TEST 1
    // Compra sin cupón y con retiro
    // =========================================================

    @Test
    void crearBoletaSinCuponYRetiroCalculaTotalCorrectamente() {

        Producto producto = crearProducto(
                2L,
                58000,
                24);

        when(productoRepository.findById(2L))
                .thenReturn(Optional.of(producto));

        CrearBoletaRequest request = crearRequest(
                null,
                "retiro",
                1);

        BoletaResponse respuesta =
                boletaService.crear(request);

        assertEquals(
                58000,
                respuesta.subtotal());

        assertEquals(
                0,
                respuesta.descuento());

        assertEquals(
                0,
                respuesta.costoEnvio());

        assertEquals(
                58000,
                respuesta.total());

        assertEquals(
                EstadoBoleta.CONFIRMADA,
                respuesta.estado());
    }

    // =========================================================
    // TEST 2
    // Cupón AROMA10
    // =========================================================

    @Test
    void cuponAroma10AplicaDiezPorCientoDeDescuento() {

        Producto producto = crearProducto(
                2L,
                58000,
                24);

        when(productoRepository.findById(2L))
                .thenReturn(Optional.of(producto));

        CrearBoletaRequest request = crearRequest(
                "AROMA10",
                "retiro",
                1);

        BoletaResponse respuesta =
                boletaService.crear(request);

        assertEquals(
                58000,
                respuesta.subtotal());

        assertEquals(
                5800,
                respuesta.descuento());

        assertEquals(
                0,
                respuesta.costoEnvio());

        assertEquals(
                52200,
                respuesta.total());
    }

    // =========================================================
    // TEST 3
    // Cupón BIENVENIDA15
    // =========================================================

    @Test
    void cuponBienvenida15AplicaQuincePorCientoDeDescuento() {

        Producto producto = crearProducto(
                2L,
                58000,
                24);

        when(productoRepository.findById(2L))
                .thenReturn(Optional.of(producto));

        CrearBoletaRequest request = crearRequest(
                "BIENVENIDA15",
                "retiro",
                1);

        BoletaResponse respuesta =
                boletaService.crear(request);

        assertEquals(
                58000,
                respuesta.subtotal());

        assertEquals(
                8700,
                respuesta.descuento());

        assertEquals(
                0,
                respuesta.costoEnvio());

        assertEquals(
                49300,
                respuesta.total());
    }

    // =========================================================
    // TEST 4
    // AROMA10 + envío a domicilio
    // =========================================================

    @Test
    void envioDomicilioAgregaCostoDe3990() {

        Producto producto = crearProducto(
                2L,
                58000,
                24);

        when(productoRepository.findById(2L))
                .thenReturn(Optional.of(producto));

        CrearBoletaRequest request = crearRequest(
                "AROMA10",
                "domicilio",
                1);

        BoletaResponse respuesta =
                boletaService.crear(request);

        /*
         * 58.000 - 5.800 + 3.990 = 56.190
         */
        assertEquals(
                58000,
                respuesta.subtotal());

        assertEquals(
                5800,
                respuesta.descuento());

        assertEquals(
                3990,
                respuesta.costoEnvio());

        assertEquals(
                56190,
                respuesta.total());
    }

    // =========================================================
    // TEST 5
    // Descuento de stock
    // =========================================================

    @Test
    void crearBoletaDescuentaStockDelProducto() {

        AtomicInteger stock =
                new AtomicInteger(24);

        Producto producto =
                crearProductoConStockVariable(
                        2L,
                        58000,
                        stock);

        when(productoRepository.findById(2L))
                .thenReturn(Optional.of(producto));

        CrearBoletaRequest request =
                crearRequest(
                        null,
                        "retiro",
                        2);

        boletaService.crear(request);

        /*
         * Stock inicial: 24
         * Compra: 2
         * Stock final: 22
         */
        assertEquals(
                22,
                stock.get());

        verify(producto)
                .setStock(22);
    }

    // =========================================================
    // TEST 6
    // Stock insuficiente
    // =========================================================

    @Test
    void stockInsuficienteLanzaReglaNegocioException() {

        Producto producto = crearProducto(
                2L,
                58000,
                21);

        when(productoRepository.findById(2L))
                .thenReturn(Optional.of(producto));

        CrearBoletaRequest request =
                crearRequest(
                        null,
                        "retiro",
                        100);

        ReglaNegocioException excepcion =
                assertThrows(
                        ReglaNegocioException.class,
                        () -> boletaService.crear(request));

        assertEquals(
                "Stock insuficiente para el producto 'Classic Wood'. Stock disponible: 21",
                excepcion.getMessage());
    }

    // =========================================================
    // TEST 7
    // Cupón inválido
    // =========================================================

    @Test
    void cuponInvalidoLanzaReglaNegocioException() {

        Producto producto = crearProducto(
                2L,
                58000,
                24);

        when(productoRepository.findById(2L))
                .thenReturn(Optional.of(producto));

        CrearBoletaRequest request =
                crearRequest(
                        "CUPONFALSO",
                        "retiro",
                        1);

        ReglaNegocioException excepcion =
                assertThrows(
                        ReglaNegocioException.class,
                        () -> boletaService.crear(request));

        assertEquals(
                "El cupón ingresado no es válido.",
                excepcion.getMessage());
    }

    // =========================================================
    // TEST 8
    // Cancelar devuelve stock
    // =========================================================

    @Test
    void cancelarBoletaDevuelveStock() {

        AtomicInteger stock =
                new AtomicInteger(24);

        Producto producto =
                crearProductoConStockVariable(
                        2L,
                        58000,
                        stock);

        when(productoRepository.findById(2L))
                .thenReturn(Optional.of(producto));

        CrearBoletaRequest crearRequest =
                crearRequest(
                        null,
                        "retiro",
                        1);

        boletaService.crear(crearRequest);

        /*
         * Después de comprar:
         * 24 -> 23
         */
        assertEquals(
                23,
                stock.get());

        /*
         * Capturamos la boleta que fue guardada
         * por el servicio.
         */
        ArgumentCaptor<Boleta> captor =
                ArgumentCaptor.forClass(
                        Boleta.class);

        verify(boletaRepository)
                .save(captor.capture());

        Boleta boletaCreada =
                captor.getValue();

        /*
         * Simulamos que esa boleta tiene ID 1
         * y que luego es encontrada para cancelarla.
         */
        when(boletaRepository.findById(1L))
                .thenReturn(
                        Optional.of(
                                boletaCreada));

        CambiarEstadoBoletaRequest cancelar =
                new CambiarEstadoBoletaRequest(
                        EstadoBoleta.CANCELADA);

        boletaService.cambiarEstado(
                1L,
                cancelar);

        /*
         * Al cancelar:
         * 23 -> 24
         */
        assertEquals(
                24,
                stock.get());

        assertEquals(
                EstadoBoleta.CANCELADA,
                boletaCreada.getEstado());
    }

    // =========================================================
    // TEST 9
    // No permitir retroceder una orden entregada
    // =========================================================

    @Test
    void noPermiteCambiarEntregadaAEnviada() {

        Boleta boleta =
                mock(Boleta.class);

        when(boleta.getEstado())
                .thenReturn(
                        EstadoBoleta.ENTREGADA);

        when(boletaRepository.findById(2L))
                .thenReturn(
                        Optional.of(
                                boleta));

        CambiarEstadoBoletaRequest request =
                new CambiarEstadoBoletaRequest(
                        EstadoBoleta.ENVIADA);

        assertThrows(
                ReglaNegocioException.class,
                () -> boletaService.cambiarEstado(
                        2L,
                        request));
    }

    // =========================================================
    // MÉTODOS AUXILIARES
    // =========================================================

    /**
     * Crea un request de boleta para evitar repetir
     * el mismo código en todas las pruebas.
     */
    private CrearBoletaRequest crearRequest(
            String cupon,
            String metodoEnvio,
            int cantidad) {

        return new CrearBoletaRequest(
                3L,
                List.of(
                        new ItemBoletaRequest(
                                2L,
                                cantidad)),
                cupon,
                "Tarjeta",
                metodoEnvio,
                metodoEnvio.equals("domicilio")
                        ? "Av. Principal 123"
                        : null,
                metodoEnvio.equals("domicilio")
                        ? "Metropolitana"
                        : null,
                metodoEnvio.equals("domicilio")
                        ? "Santiago"
                        : null,
                "912345678");
    }

    /**
     * Crea un producto simulado con un stock fijo.
     */
    private Producto crearProducto(
            Long id,
            Integer precio,
            Integer stock) {

        Producto producto =
                mock(Producto.class);

        when(producto.getId())
                .thenReturn(id);

        when(producto.getCodigo())
                .thenReturn("PF-001");

        when(producto.getNombre())
                .thenReturn("Classic Wood");

        when(producto.getPrecio())
                .thenReturn(precio);

        when(producto.getStock())
                .thenReturn(stock);

        return producto;
    }

    /**
     * Crea un producto simulado cuyo stock
     * puede cambiar durante la prueba.
     */
    private Producto crearProductoConStockVariable(
            Long id,
            Integer precio,
            AtomicInteger stock) {

        Producto producto =
                mock(Producto.class);

        when(producto.getId())
                .thenReturn(id);

        when(producto.getCodigo())
                .thenReturn("PF-001");

        when(producto.getNombre())
                .thenReturn("Classic Wood");

        when(producto.getPrecio())
                .thenReturn(precio);

        /*
         * Cada vez que el servicio consulte el stock,
         * devuelve el valor actual.
         */
        when(producto.getStock())
                .thenAnswer(
                        invocacion -> stock.get());

        /*
         * Cuando el servicio modifica el stock,
         * actualizamos nuestro AtomicInteger.
         */
        doAnswer(invocacion -> {

            Integer nuevoStock =
                    invocacion.getArgument(0);

            stock.set(nuevoStock);

            return null;

        }).when(producto)
                .setStock(any(Integer.class));

        return producto;
    }
}