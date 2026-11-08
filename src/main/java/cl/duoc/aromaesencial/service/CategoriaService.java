package cl.duoc.aromaesencial.service;

import java.util.List;

import cl.duoc.aromaesencial.dto.CategoriaRequest;
import cl.duoc.aromaesencial.dto.CategoriaResponse;
import cl.duoc.aromaesencial.exception.RecursoNoEncontradoException;
import cl.duoc.aromaesencial.exception.ReglaNegocioException;
import cl.duoc.aromaesencial.model.Categoria;
import cl.duoc.aromaesencial.repository.CategoriaRepository;
import cl.duoc.aromaesencial.repository.ProductoRepository;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de negocio de las categorías:
 * - el nombre es único (sin distinguir mayúsculas);
 * - no se puede eliminar una categoría que todavía tiene productos.
 */
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll(Sort.by("nombre")).stream()
                .map(CategoriaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtener(Long id) {
        return CategoriaResponse.from(buscar(id));
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        String nombre = request.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("Ya existe una categoría con el nombre '" + nombre + "'.", true);
        }
        Categoria categoria = Categoria.builder()
                .nombre(nombre)
                .descripcion(request.descripcion())
                .build();
        return CategoriaResponse.from(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscar(id);
        String nombre = request.nombre().trim();

        categoriaRepository.findByNombreIgnoreCase(nombre)
                .filter(otra -> !otra.getId().equals(id))
                .ifPresent(otra -> {
                    throw new ReglaNegocioException("Ya existe una categoría con el nombre '" + nombre + "'.", true);
                });

        categoria.setNombre(nombre);
        categoria.setDescripcion(request.descripcion());
        return CategoriaResponse.from(categoriaRepository.save(categoria));
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = buscar(id);
        if (productoRepository.existsByCategoriaId(id)) {
            throw new ReglaNegocioException(
                    "No se puede eliminar la categoría '" + categoria.getNombre()
                            + "' porque tiene productos asociados.", true);
        }
        categoriaRepository.delete(categoria);
    }

    private Categoria buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría", id));
    }
}
