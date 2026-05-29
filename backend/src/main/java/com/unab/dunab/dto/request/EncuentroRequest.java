package com.unab.dunab.dto.request;

import com.unab.dunab.domain.enums.EstadoEncuentro;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class EncuentroRequest {
    @NotBlank @Size(max=200)     private String nombre;
    @Size(max=2000)              private String descripcion;
    @NotBlank @Size(max=100)     private String lugar;
    @NotNull @Future             private LocalDate fecha;
    @NotNull                     private LocalTime horaInicio;
    @NotNull                     private LocalTime horaFin;
    @NotNull @Min(0)             private Integer dunabRecompensa;
    @Min(0)                      private Integer dunabPenalizacion;
    @NotNull @Min(1)             private Integer cuposMax;
    private EstadoEncuentro estado;
    private Boolean visible;
}
