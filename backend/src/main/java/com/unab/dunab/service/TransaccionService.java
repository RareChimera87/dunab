package com.unab.dunab.service;

import com.unab.dunab.dto.response.TransaccionResponse;
import com.unab.dunab.domain.enums.TipoTransaccion;
import com.unab.dunab.domain.entity.Transaccion;
import com.unab.dunab.domain.entity.Usuario;
import com.unab.dunab.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para registrar y consultar transacciones de DUNAB.
 */
@Service
@RequiredArgsConstructor
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;

    /**
     * Registra una nueva transacción para un usuario.
     */
    @Transactional
    public Transaccion registrar(Usuario usuario, TipoTransaccion tipo, int monto,
                                  String descripcion, Long referenciaId) {
        Transaccion tx = Transaccion.builder()
                .usuario(usuario)
                .tipo(tipo)
                .monto(monto)
                .balancePost(usuario.getBalanceDunab())
                .descripcion(descripcion)
                .referenciaId(referenciaId)
                .build();
        return transaccionRepository.save(tx);
    }

    /**
     * Retorna el historial de transacciones del usuario autenticado.
     */
    @Transactional(readOnly = true)
    public List<TransaccionResponse> obtenerHistorial(Long usuarioId) {
        return transaccionRepository.findByUsuarioIdOrderByCreadoEnDesc(usuarioId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retorna estadísticas de DUNAB por semana, mes, semestre y año del usuario.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas(Long usuarioId) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("promedioSemanal",  transaccionRepository.promedioSemanal(usuarioId));
        stats.put("promedioMensual",  transaccionRepository.promedioMensual(usuarioId));
        stats.put("promedioSemestral",transaccionRepository.promedioSemestral(usuarioId));
        stats.put("promedioAnual",    transaccionRepository.promedioAnual(usuarioId));
        stats.put("totalIngresos",    transaccionRepository.sumarIngresosMeses(usuarioId, 12));
        stats.put("totalEgresos",     transaccionRepository.sumarEgresosMeses(usuarioId, 12));
        return stats;
    }

    public TransaccionResponse toResponse(Transaccion t) {
        return TransaccionResponse.builder()
                .id(t.getId())
                .tipo(t.getTipo().name())
                .monto(t.getMonto())
                .balancePost(t.getBalancePost())
                .descripcion(t.getDescripcion())
                .referenciaId(t.getReferenciaId())
                .creadoEn(t.getCreadoEn())
                .build();
    }
}
