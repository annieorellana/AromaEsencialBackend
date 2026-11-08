package cl.duoc.aromaesencial.repository;

import java.util.List;

import cl.duoc.aromaesencial.model.Boleta;
import cl.duoc.aromaesencial.model.EstadoBoleta;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BoletaRepository extends JpaRepository<Boleta, Long> {

    List<Boleta> findAllByOrderByFechaDesc();

    List<Boleta> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    List<Boleta> findByEstadoOrderByFechaDesc(EstadoBoleta estado);
}
