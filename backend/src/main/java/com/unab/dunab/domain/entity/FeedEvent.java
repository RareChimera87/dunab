package com.unab.dunab.domain.entity;

import com.unab.dunab.domain.enums.FeedTipo;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento del feed de actividad global.
 * Cubre tanto eventos automáticos del sistema (inscripciones, logros, hitos)
 * como publicaciones libres de los usuarios.
 */
@Entity
@Table(name = "feed_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tipo de evento. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedTipo tipo;

    /** Usuario que generó el evento. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /**
     * Usuario objetivo (opcional).
     * Reservado para extensiones futuras como reacciones o menciones.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id")
    private Usuario target;

    /** Mensaje generado automáticamente para INSCRIPCION / LOGRO / HITO. */
    @Column(length = 255)
    private String mensaje;

    /** Cuerpo libre únicamente para PUBLICACION. */
    @Column(columnDefinition = "TEXT")
    private String cuerpo;

    /**
     * Metadata JSON opcional.
     * Ejemplo: {"encuentroId": 3, "lugar": "Cafetería del L"}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /** Soft-delete: el usuario puede borrar su propia publicación. */
    @Builder.Default
    @Column(nullable = false)
    private Boolean eliminado = false;

    @Builder.Default
    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();
}
