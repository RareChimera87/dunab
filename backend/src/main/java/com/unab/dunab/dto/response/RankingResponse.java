package com.unab.dunab.dto.response;

import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class RankingResponse {
    private Integer posicion;
    private Long    usuarioId;
    private String  nombre;
    private String  codigo;
    private String  carrera;
    private Integer balanceDunab;
    private Integer rachaDias;
    private Double  porcentajeMeta;
}
