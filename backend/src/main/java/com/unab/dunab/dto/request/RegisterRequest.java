package com.unab.dunab.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank @Size(min=3, max=120)  private String nombre;
    @NotBlank @Email                  private String correo;
    @NotBlank @Size(min=8)            private String contrasena;
    @NotBlank @Size(min=4, max=20)    private String codigo;
    @Size(max=100)                    private String carrera;
    @Size(max=150)                    private String facultad;
    @Min(1) @Max(10)                  private Integer semestre;
    @Size(max=20)                     private String celular;
    @Size(max=30)                     private String cedula;
}
