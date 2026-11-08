package cl.duoc.aromaesencial.config;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import cl.duoc.aromaesencial.model.Categoria;
import cl.duoc.aromaesencial.model.Producto;
import cl.duoc.aromaesencial.model.Rol;
import cl.duoc.aromaesencial.model.Usuario;
import cl.duoc.aromaesencial.repository.CategoriaRepository;
import cl.duoc.aromaesencial.repository.ProductoRepository;
import cl.duoc.aromaesencial.repository.UsuarioRepository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Carga datos iniciales al arrancar (solo si las tablas están vacías, por lo que es seguro reiniciar):
 * 3 categorías, los 16 perfumes del catálogo del frontend (seed/productos.json) y 3 usuarios de
 * demostración, uno por rol. Se puede desactivar con app.seed.enabled=false.
 *
 * Credenciales de demostración (SOLO desarrollo):
 *   admin@duoc.cl / admin123       -> ADMINISTRADOR
 *   vendedor@duoc.cl / vendedor123 -> VENDEDOR
 *   cliente@duoc.cl / cliente123   -> CLIENTE
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public DataSeeder(CategoriaRepository categoriaRepository, ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws IOException {
        sembrarCategorias();
        sembrarProductos();
        sembrarUsuarios();
    }

    private void sembrarCategorias() {
        if (categoriaRepository.count() > 0) {
            return;
        }
        categoriaRepository.saveAll(List.of(
                Categoria.builder().nombre("Hombre").descripcion("Fragancias masculinas").build(),
                Categoria.builder().nombre("Mujer").descripcion("Fragancias femeninas").build(),
                Categoria.builder().nombre("Unisex").descripcion("Fragancias para todos").build()));
        log.info("Seed: categorías creadas.");
    }

    private void sembrarProductos() throws IOException {
        if (productoRepository.count() > 0) {
            return;
        }
        try (InputStream in = new ClassPathResource("seed/productos.json").getInputStream()) {
            List<ProductoSeed> semillas = objectMapper.readValue(in, new TypeReference<>() {
            });
            for (ProductoSeed s : semillas) {
                Categoria categoria = categoriaRepository.findByNombreIgnoreCase(s.categoria())
                        .orElseThrow(() -> new IllegalStateException("Categoría inexistente en seed: " + s.categoria()));
                productoRepository.save(Producto.builder()
                        .codigo(s.codigo())
                        .nombre(s.nombre())
                        .descripcion(s.descripcion())
                        .precio(s.precio())
                        .stock(s.stock())
                        .stockCritico(s.stockCritico())
                        .imagen(s.imagen())
                        .oferta(s.oferta())
                        .categoria(categoria)
                        .build());
            }
            log.info("Seed: {} productos creados.", semillas.size());
        }
    }

    private void sembrarUsuarios() {
        if (usuarioRepository.count() > 0) {
            return;
        }
        usuarioRepository.save(usuario("11111111-1", "Administrador", "AromaEsencial", "admin@duoc.cl",
                "admin123", Rol.ADMINISTRADOR));
        usuarioRepository.save(usuario("22222222-2", "Vendedor", "AromaEsencial", "vendedor@duoc.cl",
                "vendedor123", Rol.VENDEDOR));
        usuarioRepository.save(usuario("33333333-3", "Cliente", "Demo", "cliente@duoc.cl",
                "cliente123", Rol.CLIENTE));
        log.info("Seed: usuarios de demostración creados (admin, vendedor, cliente).");
    }

    private Usuario usuario(String run, String nombre, String apellido, String correo, String clave, Rol rol) {
        return Usuario.builder()
                .run(run)
                .nombre(nombre)
                .apellido(apellido)
                .correo(correo)
                .password(passwordEncoder.encode(clave))
                .telefono("+56912345678")
                .fechaNacimiento(LocalDate.of(2000, 1, 1))
                .region("Región Metropolitana de Santiago")
                .comuna("Santiago")
                .direccion("Av. Libertador Bernardo O'Higgins 1000")
                .rol(rol)
                .activo(true)
                .build();
    }

    /** Estructura de cada elemento de seed/productos.json. */
    private record ProductoSeed(String codigo, String nombre, String categoria, Integer precio, Integer stock,
            Integer stockCritico, String descripcion, String imagen, boolean oferta) {
    }
}
