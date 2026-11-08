package cl.duoc.aromaesencial.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import cl.duoc.aromaesencial.dto.CategoriaRequest;
import cl.duoc.aromaesencial.dto.CategoriaResponse;
import cl.duoc.aromaesencial.exception.ReglaNegocioException;
import cl.duoc.aromaesencial.model.Categoria;
import cl.duoc.aromaesencial.repository.CategoriaRepository;
import cl.duoc.aromaesencial.repository.ProductoRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void creaUnaCategoriaNueva() {
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> {
            Categoria c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        CategoriaResponse respuesta = categoriaService.crear(new CategoriaRequest("  Infantil ", "Fragancias suaves"));

        assertEquals(10L, respuesta.id());
        assertEquals("Infantil", respuesta.nombre());
    }

    @Test
    void noPermiteNombresDuplicados() {
        when(categoriaRepository.existsByNombreIgnoreCase("Hombre")).thenReturn(true);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> categoriaService.crear(new CategoriaRequest("Hombre", null)));

        assertTrue(ex.esConflicto());
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void noEliminaUnaCategoriaConProductos() {
        Categoria hombre = Categoria.builder().id(1L).nombre("Hombre").build();
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(hombre));
        when(productoRepository.existsByCategoriaId(1L)).thenReturn(true);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> categoriaService.eliminar(1L));

        assertTrue(ex.esConflicto());
        verify(categoriaRepository, never()).delete(any());
    }

    @Test
    void eliminaUnaCategoriaVacia() {
        Categoria vacia = Categoria.builder().id(4L).nombre("Vacía").build();
        when(categoriaRepository.findById(4L)).thenReturn(Optional.of(vacia));
        when(productoRepository.existsByCategoriaId(4L)).thenReturn(false);

        categoriaService.eliminar(4L);

        verify(categoriaRepository).delete(vacia);
    }
}
