package com.unab.dunab.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Registro de un canje de recompensa realizado por un estudiante.
 * Se genera un código único para que el estudiante lo muestre al reclamar la recompensa.
 */
@Entity
@Table(name = "canjes")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Canje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recompensa_id")
    private Recompensa recompensa;

    /** Snapshot del costo al momento del canje (no cambia aunque se edite la recompensa). */
    @Column(name = "costo_dunab", nullable = false)
    private Integer costoDunab;

    /** Código único que el estudiante presenta para reclamar la recompensa. */
    @Column(name = "codigo_canje", nullable = false, unique = true, length = 20)
    private String codigoCanje;

    /**
     * Estado del canje:
     * PENDIENTE  → canjeado pero no entregado aún
     * ENTREGADO  → el admin confirmó la entrega
     * CANCELADO  → se anuló y se devolvieron los DUNAB
     */
    @Builder.Default
    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Builder.Default
    @Column(name = "canjeado_en", nullable = false)
    private LocalDateTime canjeadoEn = LocalDateTime.now();

    @Column(name = "entregado_en")
    private LocalDateTime entregadoEn;
}
