package cl.duoc.aromaesencial.service;

import java.util.List;

import cl.duoc.aromaesencial.dto.ProductoRequest;
import cl.duoc.aromaesencial.dto.ProductoResponse;
import cl.duoc.aromaesencial.exception.RecursoNoEncontradoException;
import cl.duoc.aromaesencial.exception.ReglaNegocioException;
import cl.duoc.aromaesencial.model.Categoria;
import cl.duoc.aromaesencial.model.Producto;
import cl.duoc.aromaesencial.repository.CategoriaRepository;
import cl.duoc.aromaesencial.repository.DetalleBoletaRepository;
import cl.duoc.aromaesencial.repository.ProductoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de negocio del catálogo de productos:
 * - el código es único y se guarda en mayúsculas (PF-016);
 * - la categoría debe existir;
 * - no se puede eliminar un producto que ya aparece en alguna boleta (se perdería el historial de ventas);
 * - el stock crítico se calcula como stock <= stockCritico.
 *
 * Los métodos devuelven DTOs (no entidades) y corren dentro de una transacción, porque la categoría se carga
 * de forma perezosa (lazy).
 */
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final DetalleBoletaRepository detalleBoletaRepository;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository,
            DetalleBoletaRepository detalleBoletaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.detalleBoletaRepository = detalleBoletaRepository;
    }

    /** Lista productos; ambos filtros son opcionales y pueden combinarse. */
    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(Long categoriaId, String buscar) {
        List<Producto> productos = (categoriaId != null)
                ? productoRepository.findByCategoriaId(categoriaId)
                : productoRepository.findAll();

        String texto = (buscar == null) ? "" : buscar.trim().toLowerCase();

        return productos.stream()
                .filter(p -> texto.isEmpty() || p.getNombre().toLowerCase().contains(texto))
                .map(ProductoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return ProductoResponse.from(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarOfertas() {
        return productoRepository.findByOfertaTrue().stream().map(ProductoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarStockCritico() {
        return productoRepository.findStockCritico().stream().map(ProductoResponse::from).toList();
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        String codigo = normalizarCodigo(request.codigo());
        if (productoRepository.existsByCodigo(codigo)) {
            throw new ReglaNegocioException("Ya existe un producto con el código '" + codigo + "'.", true);
        }
        Producto producto = new Producto();
        copiarDatos(producto, request, codigo, buscarCategoria(request.categoriaId()));
        return ProductoResponse.from(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = buscar(id);
        String codigo = normalizarCodigo(request.codigo());

        if (!producto.getCodigo().equals(codigo) && productoRepository.existsByCodigo(codigo)) {
            throw new ReglaNegocioException("Ya existe un producto con el código '" + codigo + "'.", true);
        }
        copiarDatos(producto, request, codigo, buscarCategoria(request.categoriaId()));
        return ProductoResponse.from(productoRepository.save(producto));
    }

    @Transactional
    public void eliminar(Long id) {
        Producto producto = buscar(id);
        if (detalleBoletaRepository.existsByProductoId(id)) {
            throw new ReglaNegocioException(
                    "No se puede eliminar el producto '" + producto.getNombre()
                            + "' porque ya fue vendido (aparece en boletas).", true);
        }
        productoRepository.delete(producto);
    }

    // ------------------------------------------------------------------ auxiliares

    private void copiarDatos(Producto producto, ProductoRequest r, String codigo, Categoria categoria) {
        producto.setCodigo(codigo);
        producto.setNombre(r.nombre().trim());
        producto.setDescripcion(r.descripcion());
        producto.setPrecio(r.precio());
        producto.setStock(r.stock());
        producto.setStockCritico(r.stockCritico());
        producto.setImagen(r.imagen());
        producto.setOferta(Boolean.TRUE.equals(r.oferta()));
        producto.setCategoria(categoria);
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase();
    }

    private Producto buscar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría", id));
    }
}
