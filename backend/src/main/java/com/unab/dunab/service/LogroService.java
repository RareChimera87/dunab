package com.unab.dunab.service;

import com.unab.dunab.domain.entity.Logro;
import com.unab.dunab.domain.entity.Usuario;
import com.unab.dunab.domain.entity.UsuarioLogro;
import com.unab.dunab.repository.InscripcionRepository;
import com.unab.dunab.repository.LogroRepository;
import com.unab.dunab.repository.UsuarioLogroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio que evalúa y otorga logros/badges a los usuarios.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogroService {

    private final LogroRepository logroRepository;
    private final UsuarioLogroRepository usuarioLogroRepository;
    private final InscripcionRepository inscripcionRepository;

    /**
     * Evalúa todos los logros para un usuario y otorga los que correspondan.
     * Llamar después de cada acreditación de DUNAB, registro de asistencia o
     * actualización de racha.
     *
     * @return lista de nuevos logros obtenidos (para mostrar notificaciones)
     */
    @Transactional
    public List<Logro> evaluarLogros(Usuario usuario) {
        // IDs de logros que ya tiene el usuario
        Set<Long> yaObtenidos = usuarioLogroRepository
                .findByUsuarioId(usuario.getId())
                .stream()
                .map(ul -> ul.getLogro().getId())
                .collect(Collectors.toSet());

        List<Logro> todos = logroRepository.findAll();
        List<Logro> nuevos = todos.stream()
                .filter(l -> !yaObtenidos.contains(l.getId()))
                .filter(l -> cumpleCondicion(l, usuario))
                .collect(Collectors.toList());

        nuevos.forEach(logro -> {
            UsuarioLogro ul = UsuarioLogro.builder()
                    .usuario(usuario)
                    .logro(logro)
                    .build();
            usuarioLogroRepository.save(ul);
            log.info("🏅 Logro desbloqueado: {} para usuario {}", logro.getCodigo(), usuario.getCorreo());
        });

        return nuevos;
    }

    private boolean cumpleCondicion(Logro logro, Usuario usuario) {
        if (logro.getCondicionTipo() == null) return false;
        int valor = logro.getCondicionValor() != null ? logro.getCondicionValor() : 0;

        return switch (logro.getCondicionTipo()) {
            case "BALANCE"    -> usuario.getBalanceDunab() >= valor;
            case "RACHA"      -> usuario.getRachaDias() >= valor;
            case "ENCUENTROS" -> inscripcionRepository.contarAsistencias(usuario.getId()) >= valor;
            // RANKING se evalúa externamente desde RankingService
            default           -> false;
        };
    }

    /**
     * Retorna todos los logros de un usuario.
     */
    @Transactional(readOnly = true)
    public List<UsuarioLogro> obtenerLogrosDeUsuario(Long usuarioId) {
        return usuarioLogroRepository.findByUsuarioId(usuarioId);
    }
}
