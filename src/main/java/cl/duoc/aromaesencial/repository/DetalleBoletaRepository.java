package cl.duoc.aromaesencial.repository;

import java.util.List;

import cl.duoc.aromaesencial.model.DetalleBoleta;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleBoletaRepository extends JpaRepository<DetalleBoleta, Long> {

    List<DetalleBoleta> findByBoletaId(Long boletaId);

    boolean existsByProductoId(Long productoId);
}
