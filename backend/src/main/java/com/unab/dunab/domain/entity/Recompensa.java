package com.unab.dunab.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Recompensa del catálogo de la tienda DUNAB.
 * El administrador crea y configura cada recompensa con su costo en DUNAB.
 */
@Entity
@Table(name = "recompensas")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recompensa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String emoji = "🎁";

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String categoria = "GENERAL";

    /** Costo en DUNAB para canjear esta recompensa. */
    @Column(name = "costo_dunab", nullable = false)
    private Integer costoDunab;

    /** Stock disponible. -1 = ilimitado. */
    @Builder.Default
    @Column(nullable = false)
    private Integer stock = -1;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activa = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;

    @Builder.Default
    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Builder.Default
    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn = LocalDateTime.now();

    /** Verifica si hay stock disponible (o es ilimitado). */
    public boolean hayStock() {
        return stock == -1 || stock > 0;
    }

    /** Descuenta una unidad de stock si es limitado. */
    public void descontarStock() {
        if (stock > 0) stock--;
    }
}
