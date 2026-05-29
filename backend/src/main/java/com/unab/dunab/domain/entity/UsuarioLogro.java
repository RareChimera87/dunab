package com.unab.dunab.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Tabla intermedia que registra los logros obtenidos por cada usuario.
 */
@Entity
@Table(
    name = "usuario_logros",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_usuario_logro",
        columnNames = {"usuario_id", "logro_id"}
    )
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioLogro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logro_id", nullable = false)
    private Logro logro;

    @Column(name = "obtenido_en", nullable = false)
    @Builder.Default
    private LocalDateTime obtenidoEn = LocalDateTime.now();
}
