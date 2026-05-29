package com.unab.dunab.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Request para crear o actualizar una recompensa en el catálogo de la tienda.
 */
@Data
public class RecompensaRequest {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres.")
    private String descripcion;

    @Size(max = 10)
    private String emoji;

    @Size(max = 50)
    private String categoria;

    @NotNull(message = "El costo en DUNAB es obligatorio.")
    @Min(value = 1, message = "El costo debe ser al menos 1 DUNAB.")
    @Max(value = 100000, message = "El costo no puede superar los 100.000 DUNAB.")
    private Integer costoDunab;

    /**
     * Stock disponible. Enviar -1 para stock ilimitado.
     */
    @Min(value = -1, message = "El stock debe ser -1 (ilimitado) o un valor positivo.")
    private Integer stock = -1;

    private Boolean activa = true;
}
