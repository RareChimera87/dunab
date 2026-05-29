package com.unab.dunab.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class InscripcionDetalleResponse {
    private Long   inscripcionId;
    private Long   usuarioId;
    private String nombre;
    private String correo;
    private String codigo;
    private Boolean asistio;
    private Boolean penalizado;
    private LocalDateTime inscritoEn;
}
