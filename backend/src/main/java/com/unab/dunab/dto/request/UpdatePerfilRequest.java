package com.unab.dunab.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdatePerfilRequest {
    @Size(min=3, max=120)   private String nombre;
    @Email                   private String correo;
    @Size(max=20)            private String celular;
    @Size(max=30)            private String cedula;
    @Size(max=100)           private String carrera;
    @Size(max=150)           private String facultad;
    @Min(1) @Max(10)         private Integer semestre;
}
