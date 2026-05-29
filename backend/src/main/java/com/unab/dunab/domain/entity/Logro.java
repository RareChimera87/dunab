package com.unab.dunab.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "logros")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Logro {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 300)
    private String descripcion;

    @Column(length = 10)
    private String emoji;

    @Column(name = "condicion_tipo", length = 50)
    private String condicionTipo;

    @Column(name = "condicion_valor")
    private Integer condicionValor;
}
