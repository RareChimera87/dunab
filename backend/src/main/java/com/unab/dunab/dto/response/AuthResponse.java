package com.unab.dunab.dto.response;

import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class AuthResponse {
    private String token;
    private String tipo;
    private Long   usuarioId;
    private String nombre;
    private String correo;
    private String rol;
    private Integer balanceDunab;
    private String  temaPreferencia;  // "CLARO" | "OSCURO" — para aplicar el tema antes del primer paint
    private String  facultad;
}
