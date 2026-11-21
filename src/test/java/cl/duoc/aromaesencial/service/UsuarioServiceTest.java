
package cl.duoc.aromaesencial.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import cl.duoc.aromaesencial.dto.UsuarioRequest;
import cl.duoc.aromaesencial.dto.UsuarioResponse;
import cl.duoc.aromaesencial.model.Rol;
import cl.duoc.aromaesencial.model.Usuario;
import cl.duoc.aromaesencial.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void preparar() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setRun("12345678-9");
        usuario.setNombre("Tamara");
        usuario.setApellido("Prueba");
        usuario.setCorreo("tamara@prueba.cl");
        usuario.setPassword("clave-cifrada");
        usuario.setTelefono("912345678");
        usuario.setFechaNacimiento(LocalDate.of(2000, 1, 1));
        usuario.setRegion("Metropolitana");
        usuario.setComuna("Santiago");
        usuario.setDireccion("Direccion de prueba");
        usuario.setRol(Rol.CLIENTE);
        usuario.setActivo(true);
    }

    private UsuarioRequest crearRequest(
            String run, String correo, String password) {

        return new UsuarioRequest(
                run,
                "Tamara",
                "Prueba",
                correo,
                password,
                "912345678",
                LocalDate.of(2000, 1, 1),
                "Metropolitana",
                "Santiago",
                "Direccion de prueba",
                Rol.CLIENTE,
                true
        );
    }

    @Test
    void listarUsuariosCorrectamente() {
        when(usuarioRepository.findAll())
                .thenReturn(List.of(usuario));

        List<UsuarioResponse> resultado =
                usuarioService.listar();

        assertEquals(1, resultado.size());
        assertEquals("Tamara", resultado.get(0).nombre());
        assertEquals("tamara@prueba.cl",
                resultado.get(0).correo());
    }

    @Test
    void buscarUsuarioPorIdCorrectamente() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        UsuarioResponse resultado =
                usuarioService.buscarPorId(1L);

        assertEquals(1L, resultado.id());
        assertEquals("12345678-9", resultado.run());
    }

    @Test
    void buscarUsuarioInexistenteRetorna404() {
        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.buscarPorId(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND,
                error.getStatusCode());
    }

    @Test
    void crearUsuarioCorrectamente() {
        UsuarioRequest request = crearRequest(
                "12345678-9",
                "tamara@prueba.cl",
                "clave123"
        );

        when(passwordEncoder.encode("clave123"))
                .thenReturn("clave-cifrada");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocacion -> {
                    Usuario guardado = invocacion.getArgument(0);
                    guardado.setId(1L);
                    return guardado;
                });

        UsuarioResponse resultado =
                usuarioService.crear(request);

        assertEquals(1L, resultado.id());
        assertEquals("Tamara", resultado.nombre());
        assertEquals(Rol.CLIENTE, resultado.rol());

        verify(passwordEncoder).encode("clave123");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void noPermiteCrearRunDuplicado() {
        UsuarioRequest request = crearRequest(
                "12345678-9",
                "nuevo@prueba.cl",
                "clave123"
        );

        when(usuarioRepository.existsByRun("12345678-9"))
                .thenReturn(true);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.crear(request)
        );

        assertEquals(HttpStatus.CONFLICT,
                error.getStatusCode());

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void noPermiteCrearCorreoDuplicado() {
        UsuarioRequest request = crearRequest(
                "98765432-1",
                "tamara@prueba.cl",
                "clave123"
        );

        when(usuarioRepository.existsByCorreoIgnoreCase(
                "tamara@prueba.cl"))
                .thenReturn(true);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.crear(request)
        );

        assertEquals(HttpStatus.CONFLICT,
                error.getStatusCode());

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void noPermiteCrearSinContrasena() {
        UsuarioRequest request = crearRequest(
                "98765432-1",
                "nuevo@prueba.cl",
                ""
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.crear(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST,
                error.getStatusCode());

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void actualizarUsuarioCorrectamente() {
        UsuarioRequest request = crearRequest(
                "12345678-9",
                "tamara@prueba.cl",
                ""
        );

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));

        UsuarioResponse resultado =
                usuarioService.actualizar(1L, request);

        assertEquals(1L, resultado.id());
        assertEquals("Tamara", resultado.nombre());

        verify(usuarioRepository).save(any(Usuario.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void actualizarConNuevaContrasena() {
        UsuarioRequest request = crearRequest(
                "12345678-9",
                "tamara@prueba.cl",
                "nuevaClave123"
        );

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.encode("nuevaClave123"))
                .thenReturn("nueva-clave-cifrada");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));

        usuarioService.actualizar(1L, request);

        assertEquals("nueva-clave-cifrada",
                usuario.getPassword());

        verify(passwordEncoder).encode("nuevaClave123");
    }

    @Test
    void eliminarUsuarioCorrectamente() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        usuarioService.eliminar(1L);

        verify(usuarioRepository).delete(usuario);
    }

    @Test
    void eliminarUsuarioInexistenteRetorna404() {
        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.eliminar(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND,
                error.getStatusCode());

        verify(usuarioRepository, never()).delete(any());
    }
}
