package com.unab.dunab.dto.response;

import com.unab.dunab.domain.entity.FeedEvent;
import com.unab.dunab.domain.enums.FeedTipo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO de respuesta para un evento del feed.
 * Expone solo los campos necesarios al frontend.
 */
@Data
public class FeedEventResponse {

    private Long id;
    private FeedTipo tipo;

    // Datos del autor
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioCodigo;
    private String usuarioAvatar; // iniciales para el avatar generado por CSS

    // Contenido del evento
    private String mensaje;  // Para INSCRIPCION / LOGRO / HITO
    private String cuerpo;   // Para PUBLICACION

    // Metadata adicional (ej: nombre del encuentro, emoji del logro…)
    private Map<String, Object> metadata;

    private LocalDateTime creadoEn;

    /** Factory: construye el DTO desde la entidad. */
    public static FeedEventResponse from(FeedEvent e) {
        FeedEventResponse r = new FeedEventResponse();
        r.setId(e.getId());
        r.setTipo(e.getTipo());
        r.setMensaje(e.getMensaje());
        r.setCuerpo(e.getCuerpo());
        r.setMetadata(e.getMetadata());
        r.setCreadoEn(e.getCreadoEn());

        if (e.getUsuario() != null) {
            r.setUsuarioId(e.getUsuario().getId());
            r.setUsuarioNombre(e.getUsuario().getNombre());
            r.setUsuarioCodigo(e.getUsuario().getCodigo());
            // Avatar: primera letra del nombre en mayúscula
            String nombre = e.getUsuario().getNombre();
            r.setUsuarioAvatar(nombre != null && !nombre.isBlank()
                    ? nombre.trim().substring(0, 1).toUpperCase() : "?");
        }
        return r;
    }
}
