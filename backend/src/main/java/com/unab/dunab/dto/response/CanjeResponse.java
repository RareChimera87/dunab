package com.unab.dunab.dto.response;

import com.unab.dunab.domain.entity.Canje;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CanjeResponse {

    private Long id;
    private String codigoCanje;
    private String estado;
    private Integer costoDunab;
    private LocalDateTime canjeadoEn;
    private LocalDateTime entregadoEn;

    // Datos de la recompensa
    private Long recompensaId;
    private String recompensaNombre;
    private String recompensaEmoji;
    private String recompensaCategoria;

    // Datos del usuario (para vista admin)
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioCodigo;

    public static CanjeResponse from(Canje c) {
        CanjeResponse dto = new CanjeResponse();
        dto.setId(c.getId());
        dto.setCodigoCanje(c.getCodigoCanje());
        dto.setEstado(c.getEstado());
        dto.setCostoDunab(c.getCostoDunab());
        dto.setCanjeadoEn(c.getCanjeadoEn());
        dto.setEntregadoEn(c.getEntregadoEn());

        if (c.getRecompensa() != null) {
            dto.setRecompensaId(c.getRecompensa().getId());
            dto.setRecompensaNombre(c.getRecompensa().getNombre());
            dto.setRecompensaEmoji(c.getRecompensa().getEmoji());
            dto.setRecompensaCategoria(c.getRecompensa().getCategoria());
        }
        if (c.getUsuario() != null) {
            dto.setUsuarioId(c.getUsuario().getId());
            dto.setUsuarioNombre(c.getUsuario().getNombre());
            dto.setUsuarioCodigo(c.getUsuario().getCodigo());
        }
        return dto;
    }
}
