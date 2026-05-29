package com.unab.dunab.service;

import com.unab.dunab.dto.request.FeedPublicacionRequest;
import com.unab.dunab.dto.response.FeedEventResponse;
import com.unab.dunab.common.exception.DunabException;
import com.unab.dunab.domain.entity.Encuentro;
import com.unab.dunab.domain.entity.FeedEvent;
import com.unab.dunab.domain.enums.FeedTipo;
import com.unab.dunab.domain.entity.Logro;
import com.unab.dunab.domain.entity.Usuario;
import com.unab.dunab.repository.FeedEventRepository;
import com.unab.dunab.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Servicio del feed de actividad global.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Publicar eventos automáticos del sistema (inscripciones, logros, hitos).</li>
 *   <li>Publicar mensajes libres de los usuarios.</li>
 *   <li>Consultar el feed con paginación.</li>
 *   <li>Eliminar publicaciones propias (soft-delete).</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    /** Número de eventos por página en el feed completo. */
    private static final int PAGE_SIZE = 20;

    /** Número de eventos para el mini-feed del dashboard. */
    private static final int MINI_FEED_SIZE = 6;

    /** Hitos de saldo que generan un evento en el feed. */
    private static final int[] HITOS_DUNAB = {1_000, 2_500, 5_000, 7_500, 10_000};

    private final FeedEventRepository feedEventRepository;
    private final UsuarioRepository usuarioRepository;

    /* ───────────────────────────────────────────────────────────────────────
     *  PUBLICACIÓN DE EVENTOS (llamados desde otros servicios)
     * ─────────────────────────────────────────────────────────────────────── */

    /**
     * Publica un evento de inscripción a un encuentro.
     * Llamar desde {@code InscripcionService} al confirmar la inscripción.
     */
    @Transactional
    public void publicarInscripcion(Usuario usuario, Encuentro encuentro) {
        String msg = usuario.getNombre() + " se inscribió en \"" + encuentro.getNombre() + "\"";
        FeedEvent event = FeedEvent.builder()
                .tipo(FeedTipo.INSCRIPCION)
                .usuario(usuario)
                .mensaje(msg)
                .metadata(Map.of(
                        "encuentroId", encuentro.getId(),
                        "lugar",       encuentro.getLugar(),
                        "nombre",      encuentro.getNombre()
                ))
                .build();
        feedEventRepository.save(event);
        log.debug("Feed INSCRIPCION: {}", msg);
    }

    /**
     * Publica un evento de logro desbloqueado.
     * Llamar desde {@code LogroService#evaluarLogros} para cada logro nuevo.
     */
    @Transactional
    public void publicarLogro(Usuario usuario, Logro logro) {
        String msg = usuario.getNombre() + " desbloqueó el logro \"" + logro.getNombre() + "\"";
        FeedEvent event = FeedEvent.builder()
                .tipo(FeedTipo.LOGRO)
                .usuario(usuario)
                .mensaje(msg)
                .metadata(Map.of(
                        "logroId",    logro.getId(),
                        "logroCodigo", logro.getCodigo(),
                        "emoji",      logro.getEmoji() != null ? logro.getEmoji() : "🏅"
                ))
                .build();
        feedEventRepository.save(event);
        log.debug("Feed LOGRO: {}", msg);
    }

    /**
     * Evalúa si el saldo actual del usuario supera algún hito y,
     * de ser así, publica el evento correspondiente (una sola vez).
     * Llamar desde {@code TransaccionService} después de cada acreditación.
     */
    @Transactional
    public void evaluarYPublicarHito(Usuario usuario) {
        for (int hito : HITOS_DUNAB) {
            if (usuario.getBalanceDunab() >= hito) {
                String hitoStr = formatearHito(hito);
                // Solo publicamos si no existe un hito idéntico previo
                boolean yaExiste = feedEventRepository
                        .findHitoExistente(usuario.getId(), hitoStr)
                        .isPresent();
                if (!yaExiste) {
                    String msg = usuario.getNombre() + " alcanzó " + hitoStr + " DUNAB 🎉";
                    FeedEvent event = FeedEvent.builder()
                            .tipo(FeedTipo.HITO)
                            .usuario(usuario)
                            .mensaje(msg)
                            .metadata(Map.of("hito", hito))
                            .build();
                    feedEventRepository.save(event);
                    log.debug("Feed HITO: {}", msg);
                }
            }
        }
    }

    /* ───────────────────────────────────────────────────────────────────────
     *  PUBLICACIONES DE USUARIO
     * ─────────────────────────────────────────────────────────────────────── */

    /**
     * Crea una publicación libre del usuario en el feed.
     */
    @Transactional
    public FeedEventResponse publicar(String correo, FeedPublicacionRequest req) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));

        FeedEvent event = FeedEvent.builder()
                .tipo(FeedTipo.PUBLICACION)
                .usuario(usuario)
                .cuerpo(req.getCuerpo().trim())
                .build();
        return FeedEventResponse.from(feedEventRepository.save(event));
    }

    /**
     * Elimina (soft-delete) una publicación propia.
     * Solo el autor o un ADMIN puede borrar.
     */
    @Transactional
    public void eliminar(Long eventId, String correoSolicitante) {
        FeedEvent event = feedEventRepository.findById(eventId)
                .orElseThrow(() -> DunabException.notFound("Evento no encontrado"));

        Usuario solicitante = usuarioRepository.findByCorreo(correoSolicitante)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));

        boolean esAutor = event.getUsuario().getId().equals(solicitante.getId());
        boolean esAdmin = solicitante.getRol().name().contains("ADMIN");

        if (!esAutor && !esAdmin) {
            throw DunabException.forbidden("No tienes permiso para eliminar este evento.");
        }

        event.setEliminado(true);
        feedEventRepository.save(event);
    }

    /* ───────────────────────────────────────────────────────────────────────
     *  CONSULTAS
     * ─────────────────────────────────────────────────────────────────────── */

    /**
     * Retorna una página del feed global.
     *
     * @param tipo  Filtro opcional por tipo de evento. {@code null} = todos.
     * @param page  Número de página (0-based).
     */
    @Transactional(readOnly = true)
    public Page<FeedEventResponse> obtenerFeed(FeedTipo tipo, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<FeedEvent> eventos = (tipo == null)
                ? feedEventRepository.findFeedGlobal(pageable)
                : feedEventRepository.findFeedByTipo(tipo, pageable);
        return eventos.map(FeedEventResponse::from);
    }

    /**
     * Retorna los últimos {@value #MINI_FEED_SIZE} eventos para el widget del dashboard.
     */
    @Transactional(readOnly = true)
    public List<FeedEventResponse> obtenerMiniFeed() {
        return feedEventRepository
                .findFeedGlobal(PageRequest.of(0, MINI_FEED_SIZE))
                .map(FeedEventResponse::from)
                .toList();
    }

    /* ───────────────────────────────────────────────────────────────────────
     *  HELPERS
     * ─────────────────────────────────────────────────────────────────────── */

    private String formatearHito(int hito) {
        if (hito >= 1_000) {
            int k = hito / 1_000;
            int resto = hito % 1_000;
            return resto == 0 ? k + "K" : (hito / 100) / 10.0 + "K";
        }
        return String.valueOf(hito);
    }
}
