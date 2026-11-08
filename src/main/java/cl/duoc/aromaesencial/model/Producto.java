package cl.duoc.aromaesencial.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla PRODUCTO. Equivale a los objetos de src/data/productos.js del frontend:
 * el "id" tipo slug ('dark-cedar') pasa a ser un id numérico y el "codigo" ('PF-016') queda como código
 * de negocio único. El precio está en pesos chilenos (CLP, sin decimales).
 */
@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Integer precio;

    @Column(nullable = false)
    private Integer stock;

    /** Cuando stock <= stockCritico el producto aparece en el reporte de stock crítico. */
    @Column(name = "stock_critico", nullable = false)
    private Integer stockCritico;

    @Column(length = 500)
    private String imagen;

    @Column(nullable = false)
    @Builder.Default
    private Boolean oferta = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;
}
