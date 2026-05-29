package com.unab.dunab.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class EncuentroResponse {
    private Long    id;
    private String  nombre;
    private String  descripcion;
    private String  lugar;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer dunabRecompensa;
    private Integer dunabPenalizacion;
    private Integer cuposMax;
    private Integer cuposOcupados;
    private Integer cuposLibres;
    private String  estado;
    private Boolean visible;
    private Boolean inscrito;   // indica si el usuario actual está inscrito
}
