package cl.duoc.aromaesencial.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla BOLETA (la "orden" del frontend). Un usuario tiene muchas boletas y una boleta tiene muchos
 * detalles. Los datos de despacho se copian a la boleta (snapshot) para que no cambien si el usuario
 * modifica después su perfil.
 */
@Entity
@Table(name = "boleta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Boleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoBoleta estado = EstadoBoleta.CONFIRMADA;

    @Column(nullable = false)
    private Integer subtotal;

    @Column(nullable = false)
    @Builder.Default
    private Integer descuento = 0;

    @Column(name = "costo_envio", nullable = false)
    @Builder.Default
    private Integer costoEnvio = 0;

    @Column(nullable = false)
    private Integer total;

    @Column(length = 30)
    private String cupon;

    @Column(name = "metodo_pago", length = 40)
    private String metodoPago;

    /** "domicilio" o "retiro" (igual que el checkout del frontend). */
    @Column(name = "metodo_envio", length = 20)
    private String metodoEnvio;

    @Column(name = "direccion_envio", length = 200)
    private String direccionEnvio;

    @Column(length = 80)
    private String region;

    @Column(length = 80)
    private String comuna;

    @Column(length = 20)
    private String telefono;

    @OneToMany(mappedBy = "boleta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleBoleta> detalles = new ArrayList<>();

    /** Mantiene sincronizados ambos lados de la relación. */
    public void agregarDetalle(DetalleBoleta detalle) {
        detalle.setBoleta(this);
        this.detalles.add(detalle);
    }
}
