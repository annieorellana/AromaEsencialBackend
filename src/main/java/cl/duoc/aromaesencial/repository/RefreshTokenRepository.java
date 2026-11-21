package cl.duoc.aromaesencial.repository;

import cl.duoc.aromaesencial.model.RefreshToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    // Consulta normal por hash
    Optional<RefreshToken> findByTokenHash(
            String tokenHash
    );

    // Bloquea el registro mientras se renueva
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM RefreshToken r
        WHERE r.tokenHash = :hash
    """)
    Optional<RefreshToken> buscarParaRotacion(
            @Param("hash") String hash
    );
}
