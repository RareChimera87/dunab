package com.unab.dunab.domain.entity;

import com.unab.dunab.domain.enums.Rol;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa a un estudiante o administrador del sistema DUNAB.
 */
@Entity
@Table(name = "usuarios")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 120)
    private String correo;

    @Column(nullable = false, length = 255)
    private String contrasena;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(length = 30)
    private String cedula;

    @Column(length = 20)
    private String celular;

    @Column(length = 100)
    private String carrera;

    @Column(length = 150)
    @Builder.Default
    private String facultad = "Facultad de Ciencias Jurídicas y Políticas";

    @Column(nullable = false)
    @Builder.Default
    private Integer semestre = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Rol rol = Rol.ESTUDIANTE;

    @Column(name = "balance_dunab", nullable = false)
    @Builder.Default
    private Integer balanceDunab = 0;

    @Column(name = "racha_dias", nullable = false)
    @Builder.Default
    private Integer rachaDias = 0;

    @Column(name = "ultima_actividad")
    private LocalDate ultimaActividad;

    /** PIN de 4 dígitos hasheado con BCrypt. Null si el usuario aún no lo ha configurado. */
    @Column(name = "pin_seguridad", length = 255)
    private String pinSeguridad;

    /** Preferencia de tema de UI: "CLARO" o "OSCURO". Persistida por usuario. */
    @Column(name = "tema_preferencia", nullable = false, length = 10)
    @Builder.Default
    private String temaPreferencia = "CLARO";

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime creadoEn = LocalDateTime.now();

    @UpdateTimestamp
    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    /* ── Relaciones ── */

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Inscripcion> inscripciones = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Transaccion> transacciones = new ArrayList<>();

    /* ── Métodos de dominio ── */

    public void acreditarDunab(int monto) {
        this.balanceDunab += monto;
    }

    public void debitarDunab(int monto) {
        if (this.balanceDunab < monto) {
            throw new IllegalArgumentException("Saldo insuficiente de DUNAB.");
        }
        this.balanceDunab -= monto;
    }

    public boolean tieneSaldo(int monto) {
        return this.balanceDunab >= monto;
    }
}
