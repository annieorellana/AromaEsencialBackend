package cl.duoc.aromaesencial.repository;

import java.util.List;
import java.util.Optional;

import cl.duoc.aromaesencial.model.Rol;
import cl.duoc.aromaesencial.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByRun(String run);

    List<Usuario> findByRol(Rol rol);
}
