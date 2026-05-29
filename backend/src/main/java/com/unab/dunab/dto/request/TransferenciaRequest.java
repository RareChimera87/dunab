package com.unab.dunab.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TransferenciaRequest {
    @NotNull                        private Long destinatarioId;
    @NotNull @Min(1) @Max(2000)     private Integer monto;
    @Size(max=300)                  private String nota;
}
