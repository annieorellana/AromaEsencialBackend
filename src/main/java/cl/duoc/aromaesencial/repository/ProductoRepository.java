package cl.duoc.aromaesencial.repository;

import java.util.List;
import java.util.Optional;

import cl.duoc.aromaesencial.model.Producto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Producto> findByCategoriaId(Long categoriaId);

    List<Producto> findByOfertaTrue();

    List<Producto> findByNombreContainingIgnoreCase(String texto);

    boolean existsByCategoriaId(Long categoriaId);

    /** Productos con stock crítico (stock <= stock_critico), para el reporte del administrador. */
    @Query("SELECT p FROM Producto p WHERE p.stock <= p.stockCritico ORDER BY p.stock ASC")
    List<Producto> findStockCritico();
}
