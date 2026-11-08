package cl.duoc.aromaesencial.dto;

import cl.duoc.aromaesencial.model.Categoria;

/** Categoría tal como se devuelve al cliente. */
public record CategoriaResponse(Long id, String nombre, String descripcion) {

    public static CategoriaResponse from(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNombre(), categoria.getDescripcion());
    }
}
