package com.unab.dunab.dto.response;

import com.unab.dunab.domain.entity.Recompensa;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecompensaResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private String emoji;
    private String categoria;
    private Integer costoDunab;
    private Integer stock;           // -1 = ilimitado
    private Boolean activa;
    private LocalDateTime creadoEn;

    public static RecompensaResponse from(Recompensa r) {
        RecompensaResponse dto = new RecompensaResponse();
        dto.setId(r.getId());
        dto.setNombre(r.getNombre());
        dto.setDescripcion(r.getDescripcion());
        dto.setEmoji(r.getEmoji());
        dto.setCategoria(r.getCategoria());
        dto.setCostoDunab(r.getCostoDunab());
        dto.setStock(r.getStock());
        dto.setActiva(r.getActiva());
        dto.setCreadoEn(r.getCreadoEn());
        return dto;
    }
}
