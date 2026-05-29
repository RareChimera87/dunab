package com.unab.dunab.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inscripciones",
       uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id","encuentro_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Inscripcion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encuentro_id", nullable = false)
    private Encuentro encuentro;

    private Boolean asistio;

    @Column(nullable = false) @Builder.Default
    private Boolean penalizado = false;

    @Column(name = "inscrito_en", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime inscritoEn = LocalDateTime.now();
}
