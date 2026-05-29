package com.unab.dunab.service;

import com.unab.dunab.dto.response.RankingResponse;
import com.unab.dunab.common.exception.DunabException;
import com.unab.dunab.domain.entity.Logro;
import com.unab.dunab.domain.entity.Usuario;
import com.unab.dunab.domain.entity.UsuarioLogro;
import com.unab.dunab.repository.LogroRepository;
import com.unab.dunab.repository.UsuarioLogroRepository;
import com.unab.dunab.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Servicio del ranking de estudiantes por balance de DUNAB.
 * También evalúa logros de tipo RANKING.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private static final int META_DUNAB = 10_000;

    private final UsuarioRepository usuarioRepository;
    private final UsuarioLogroRepository usuarioLogroRepository;
    private final LogroRepository logroRepository;

    /* ─── Ranking completo ───────────────────────────────── */

    @Transactional(readOnly = true)
    public List<RankingResponse> obtenerRanking() {
        List<Usuario> usuarios = usuarioRepository.findRanking();
        return buildRanking(usuarios);
    }

    @Transactional(readOnly = true)
    public List<RankingResponse> obtenerTop(int limite) {
        List<Usuario> usuarios = usuarioRepository.findTopN(PageRequest.of(0, limite));
        return buildRanking(usuarios);
    }

    /* ─── Posición de un usuario ─────────────────────────── */

    @Transactional(readOnly = true)
    public Map<String, Object> miPosicion(String correo) {
        List<Usuario> ranking = usuarioRepository.findRanking();
        Usuario yo = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));

        int posicion = IntStream.range(0, ranking.size())
                .filter(i -> ranking.get(i).getId().equals(yo.getId()))
                .map(i -> i + 1)
                .findFirst()
                .orElse(ranking.size() + 1);

        double pct = Math.min(100.0, (yo.getBalanceDunab() * 100.0) / META_DUNAB);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("posicion",      posicion);
        result.put("totalUsuarios", ranking.size());
        result.put("balanceDunab",  yo.getBalanceDunab());
        result.put("porcentajeMeta", Math.round(pct * 10.0) / 10.0);
        result.put("faltanDunab",   Math.max(0, META_DUNAB - yo.getBalanceDunab()));
        return result;
    }

    /* ─── Evaluación de logros de ranking ─────────────────── */

    /**
     * Evalúa y otorga logros de tipo RANKING a todos los usuarios.
     * Llamar periódicamente o después de cada cambio de balance masivo.
     */
    @Transactional
    public void evaluarLogrosRanking() {
        List<Usuario> ranking = usuarioRepository.findRanking();
        List<Logro> logrosRanking = logroRepository.findAll()
                .stream()
                .filter(l -> "RANKING".equals(l.getCondicionTipo()))
                .collect(Collectors.toList());

        IntStream.range(0, ranking.size()).forEach(i -> {
            Usuario u = ranking.get(i);
            int posicion = i + 1;

            Set<Long> yaObtenidos = usuarioLogroRepository.findByUsuarioId(u.getId())
                    .stream().map(ul -> ul.getLogro().getId()).collect(Collectors.toSet());

            logrosRanking.stream()
                    .filter(l -> !yaObtenidos.contains(l.getId()))
                    .filter(l -> l.getCondicionValor() != null && posicion <= l.getCondicionValor())
                    .forEach(logro -> {
                        UsuarioLogro ul = UsuarioLogro.builder()
                                .usuario(u).logro(logro).build();
                        usuarioLogroRepository.save(ul);
                        log.info("🏅 Logro RANKING desbloqueado: {} para {} (posición {})",
                                logro.getCodigo(), u.getCorreo(), posicion);
                    });
        });
    }

    /* ─── Builder interno ────────────────────────────────── */

    private List<RankingResponse> buildRanking(List<Usuario> usuarios) {
        return IntStream.range(0, usuarios.size())
                .mapToObj(i -> {
                    Usuario u = usuarios.get(i);
                    double pct = Math.min(100.0, (u.getBalanceDunab() * 100.0) / META_DUNAB);
                    return RankingResponse.builder()
                            .posicion(i + 1)
                            .usuarioId(u.getId())
                            .nombre(u.getNombre())
                            .codigo(u.getCodigo())
                            .carrera(u.getCarrera())
                            .balanceDunab(u.getBalanceDunab())
                            .rachaDias(u.getRachaDias())
                            .porcentajeMeta(Math.round(pct * 10.0) / 10.0)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
