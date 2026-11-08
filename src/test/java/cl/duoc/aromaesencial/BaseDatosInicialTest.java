package cl.duoc.aromaesencial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cl.duoc.aromaesencial.model.Rol;
import cl.duoc.aromaesencial.model.Usuario;
import cl.duoc.aromaesencial.repository.CategoriaRepository;
import cl.duoc.aromaesencial.repository.ProductoRepository;
import cl.duoc.aromaesencial.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Verifica que el contexto arranca, que las tablas se crean y que el seed deja el sistema usable. */
@SpringBootTest
class BaseDatosInicialTest {

    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void cargaCategoriasYProductosDelCatalogo() {
        assertEquals(3, categoriaRepository.count());
        assertEquals(16, productoRepository.count());
        assertTrue(productoRepository.existsByCodigo("PF-016"));
    }

    @Test
    void detectaProductosConStockCritico() {
        assertFalse(productoRepository.findStockCritico().isEmpty());
        productoRepository.findStockCritico()
                .forEach(p -> assertTrue(p.getStock() <= p.getStockCritico()));
    }

    @Test
    void creaUnUsuarioPorRolConClaveCifrada() {
        for (Rol rol : Rol.values()) {
            assertEquals(1, usuarioRepository.findByRol(rol).size(), "Falta usuario con rol " + rol);
        }
        Usuario admin = usuarioRepository.findByCorreoIgnoreCase("ADMIN@duoc.cl").orElseThrow();
        assertNotEquals("admin123", admin.getPassword());
        assertTrue(passwordEncoder.matches("admin123", admin.getPassword()));
    }
}
