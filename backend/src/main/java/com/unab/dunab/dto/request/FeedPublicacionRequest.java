package com.unab.dunab.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request para que un usuario publique en el feed.
 */
@Data
public class FeedPublicacionRequest {

    @NotBlank(message = "El contenido no puede estar vacío.")
    @Size(min = 1, max = 280, message = "La publicación debe tener entre 1 y 280 caracteres.")
    private String cuerpo;
}
