package com.unab.dunab.domain.entity;

import com.unab.dunab.domain.enums.EstadoEncuentro;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un encuentro donde los estudiantes ganan DUNAB.
 * Lugares posibles: Cafetería del L, Clase, Play en Banú, Biblioteca, CSU.
 */
@Entity
@Table(name = "encuentros")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Encuentro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, length = 100)
    private String lugar;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "dunab_recompensa", nullable = false)
    @Builder.Default
    private Integer dunabRecompensa = 0;

    @Column(name = "dunab_penalizacion", nullable = false)
    @Builder.Default
    private Integer dunabPenalizacion = 0;

    @Column(name = "cupos_max", nullable = false)
    @Builder.Default
    private Integer cuposMax = 30;

    @Column(name = "cupos_ocupados", nullable = false)
    @Builder.Default
    private Integer cuposOcupados = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoEncuentro estado = EstadoEncuentro.ACTIVO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean visible = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;

    @Column(name = "creado_en", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime creadoEn = LocalDateTime.now();

    @UpdateTimestamp
    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    /* ── Relaciones ── */

    @OneToMany(mappedBy = "encuentro", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Inscripcion> inscripciones = new ArrayList<>();

    /* ── Métodos de dominio ── */

    public boolean tieneCuposDisponibles() {
        return this.cuposOcupados < this.cuposMax;
    }

    public int getCuposLibres() {
        return this.cuposMax - this.cuposOcupados;
    }

    public void incrementarCupos() {
        if (!tieneCuposDisponibles()) {
            throw new IllegalStateException("El encuentro no tiene cupos disponibles.");
        }
        this.cuposOcupados++;
        if (this.cuposOcupados >= this.cuposMax) {
            this.estado = EstadoEncuentro.LLENO;
        }
    }

    public void decrementarCupos() {
        if (this.cuposOcupados > 0) {
            this.cuposOcupados--;
            if (this.estado == EstadoEncuentro.LLENO) {
                this.estado = EstadoEncuentro.ACTIVO;
            }
        }
    }
}
