package com.unab.dunab.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank @Email  private String correo;
    @NotBlank         private String contrasena;
}
