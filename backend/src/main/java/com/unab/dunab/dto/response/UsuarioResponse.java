package com.unab.dunab.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class UsuarioResponse {
    private Long          id;
    private String        nombre;
    private String        correo;
    private String        codigo;
    private String        cedula;
    private String        celular;
    private String        carrera;
    private String        facultad;
    private Integer       semestre;
    private String        rol;
    private Integer       balanceDunab;
    private Integer       rachaDias;
    private LocalDate     ultimaActividad;
    private Boolean       activo;
    private String        temaPreferencia;  // "CLARO" | "OSCURO"
    private LocalDateTime creadoEn;
}
