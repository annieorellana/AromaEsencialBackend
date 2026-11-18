package cl.duoc.aromaesencial.service;

import java.time.LocalDateTime;
import java.util.List;

import cl.duoc.aromaesencial.dto.BoletaResponse;
import cl.duoc.aromaesencial.dto.CambiarEstadoBoletaRequest;
import cl.duoc.aromaesencial.dto.CrearBoletaRequest;
import cl.duoc.aromaesencial.dto.ItemBoletaRequest;
import cl.duoc.aromaesencial.exception.RecursoNoEncontradoException;
import cl.duoc.aromaesencial.exception.ReglaNegocioException;
import cl.duoc.aromaesencial.model.Boleta;
import cl.duoc.aromaesencial.model.DetalleBoleta;
import cl.duoc.aromaesencial.model.EstadoBoleta;
import cl.duoc.aromaesencial.model.Producto;
import cl.duoc.aromaesencial.model.Usuario;
import cl.duoc.aromaesencial.repository.BoletaRepository;
import cl.duoc.aromaesencial.repository.ProductoRepository;
import cl.duoc.aromaesencial.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de negocio de las boletas / órdenes.
 *
 * Se encarga de:
 * - validar usuario y productos;
 * - validar y descontar stock;
 * - calcular subtotal, descuentos, envío y total;
 * - guardar el precio del producto al momento de la compra;
 * - administrar los estados de la orden;
 * - devolver el stock cuando una orden es cancelada.
 */
@Service
public class BoletaService {

    private static final int COSTO_ENVIO_DOMICILIO = 3990;

    private static final String CUPON_AROMA10 = "AROMA10";
    private static final String CUPON_BIENVENIDA15 = "BIENVENIDA15";

    private final BoletaRepository boletaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public BoletaService(
            BoletaRepository boletaRepository,
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository) {

        this.boletaRepository = boletaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ------------------------------------------------------------------
    // CREAR BOLETA
    // ------------------------------------------------------------------

    @Transactional
    public BoletaResponse crear(CrearBoletaRequest request) {

        Usuario usuario = buscarUsuario(request.usuarioId());

        validarMetodoEnvio(request);

        int subtotal = 0;

        Boleta boleta = Boleta.builder()
                .usuario(usuario)
                .fecha(LocalDateTime.now())
                .estado(EstadoBoleta.CONFIRMADA)
                .cupon(normalizarCupon(request.cupon()))
                .metodoPago(request.metodoPago().trim())
                .metodoEnvio(request.metodoEnvio().trim().toLowerCase())
                .direccionEnvio(request.direccionEnvio())
                .region(request.region())
                .comuna(request.comuna())
                .telefono(request.telefono())
                .build();

        for (ItemBoletaRequest item : request.items()) {

            Producto producto = buscarProducto(item.productoId());

            validarStock(producto, item.cantidad());

            int subtotalDetalle = producto.getPrecio() * item.cantidad();

            DetalleBoleta detalle = DetalleBoleta.builder()
                    .producto(producto)
                    .cantidad(item.cantidad())
                    .precioUnitario(producto.getPrecio())
                    .subtotal(subtotalDetalle)
                    .build();

            boleta.agregarDetalle(detalle);

            subtotal += subtotalDetalle;

            // Descontar stock
            producto.setStock(producto.getStock() - item.cantidad());
        }

        int descuento = calcularDescuento(
                subtotal,
                boleta.getCupon());

        int costoEnvio = calcularCostoEnvio(
                request.metodoEnvio());

        int total = subtotal - descuento + costoEnvio;

        boleta.setSubtotal(subtotal);
        boleta.setDescuento(descuento);
        boleta.setCostoEnvio(costoEnvio);
        boleta.setTotal(total);

        Boleta guardada = boletaRepository.save(boleta);

        return BoletaResponse.from(guardada);
    }

    // ------------------------------------------------------------------
    // LISTAR
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<BoletaResponse> listar() {

        return boletaRepository.findAllByOrderByFechaDesc()
                .stream()
                .map(BoletaResponse::from)
                .toList();
    }

    // ------------------------------------------------------------------
    // LISTAR POR USUARIO
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<BoletaResponse> listarPorUsuario(Long usuarioId) {

        buscarUsuario(usuarioId);

        return boletaRepository.findByUsuarioIdOrderByFechaDesc(usuarioId)
                .stream()
                .map(BoletaResponse::from)
                .toList();
    }

    // ------------------------------------------------------------------
    // OBTENER DETALLE
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public BoletaResponse obtener(Long id) {

        return BoletaResponse.from(buscarBoleta(id));
    }

    // ------------------------------------------------------------------
    // CAMBIAR ESTADO
    // ------------------------------------------------------------------

    @Transactional
    public BoletaResponse cambiarEstado(
            Long id,
            CambiarEstadoBoletaRequest request) {

        Boleta boleta = buscarBoleta(id);

        EstadoBoleta estadoActual = boleta.getEstado();
        EstadoBoleta nuevoEstado = request.estado();

        if (estadoActual == nuevoEstado) {
            return BoletaResponse.from(boleta);
        }

        validarCambioEstado(estadoActual, nuevoEstado);

        if (nuevoEstado == EstadoBoleta.CANCELADA) {
            devolverStock(boleta);
        }

        boleta.setEstado(nuevoEstado);

        return BoletaResponse.from(
                boletaRepository.save(boleta));
    }

    // ------------------------------------------------------------------
    // AUXILIARES
    // ------------------------------------------------------------------

    private Usuario buscarUsuario(Long id) {

        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Usuario",
                                id));
    }

    private Producto buscarProducto(Long id) {

        return productoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Producto",
                                id));
    }

    private Boleta buscarBoleta(Long id) {

        return boletaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Boleta",
                                id));
    }

    private void validarStock(
            Producto producto,
            Integer cantidad) {

        if (producto.getStock() < cantidad) {

            throw new ReglaNegocioException(
                    "Stock insuficiente para el producto '"
                            + producto.getNombre()
                            + "'. Stock disponible: "
                            + producto.getStock());
        }
    }

    private int calcularDescuento(
            int subtotal,
            String cupon) {

        if (cupon == null || cupon.isBlank()) {
            return 0;
        }

        return switch (cupon) {

            case CUPON_AROMA10 ->
                    (int) Math.round(subtotal * 0.10);

            case CUPON_BIENVENIDA15 ->
                    (int) Math.round(subtotal * 0.15);

            default ->
                    throw new ReglaNegocioException(
                            "El cupón ingresado no es válido.");
        };
    }

    private int calcularCostoEnvio(
            String metodoEnvio) {

        if (metodoEnvio.equalsIgnoreCase("domicilio")) {
            return COSTO_ENVIO_DOMICILIO;
        }

        if (metodoEnvio.equalsIgnoreCase("retiro")) {
            return 0;
        }

        throw new ReglaNegocioException(
                "El método de envío debe ser 'domicilio' o 'retiro'.");
    }

    private String normalizarCupon(String cupon) {

        if (cupon == null || cupon.isBlank()) {
            return null;
        }

        return cupon.trim().toUpperCase();
    }

    private void validarMetodoEnvio(
            CrearBoletaRequest request) {

        String metodo = request.metodoEnvio();

        if (metodo.equalsIgnoreCase("domicilio")) {

            if (estaVacio(request.direccionEnvio())
                    || estaVacio(request.region())
                    || estaVacio(request.comuna())) {

                throw new ReglaNegocioException(
                        "Para envío a domicilio debe indicar dirección, región y comuna.");
            }
        }
    }

    private boolean estaVacio(String texto) {

        return texto == null || texto.isBlank();
    }

    private void devolverStock(Boleta boleta) {

        for (DetalleBoleta detalle : boleta.getDetalles()) {

            Producto producto = detalle.getProducto();

            producto.setStock(
                    producto.getStock()
                            + detalle.getCantidad());
        }
    }

    private void validarCambioEstado(
            EstadoBoleta actual,
            EstadoBoleta nuevo) {

        boolean valido = switch (actual) {

            case CONFIRMADA ->
                    nuevo == EstadoBoleta.EN_PREPARACION
                            || nuevo == EstadoBoleta.CANCELADA;

            case EN_PREPARACION ->
                    nuevo == EstadoBoleta.ENVIADA
                            || nuevo == EstadoBoleta.CANCELADA;

            case ENVIADA ->
                    nuevo == EstadoBoleta.ENTREGADA
                            || nuevo == EstadoBoleta.CANCELADA;

            case ENTREGADA, CANCELADA -> false;
        };

        if (!valido) {

            throw new ReglaNegocioException(
                    "No se puede cambiar una boleta de "
                            + actual.getEtiqueta()
                            + " a "
                            + nuevo.getEtiqueta()
                            + ".");
        }
    }
}