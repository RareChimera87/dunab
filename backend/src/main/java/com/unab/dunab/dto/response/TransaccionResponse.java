package com.unab.dunab.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class TransaccionResponse {
    private Long          id;
    private String        tipo;
    private Integer       monto;
    private Integer       balancePost;
    private String        descripcion;
    private Long          referenciaId;
    private LocalDateTime creadoEn;
}
