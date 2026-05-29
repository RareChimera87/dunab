package com.unab.dunab.service;

import com.unab.dunab.dto.request.EncuentroRequest;
import com.unab.dunab.dto.response.EncuentroResponse;
import com.unab.dunab.dto.response.InscripcionDetalleResponse;
import com.unab.dunab.common.exception.DunabException;
import com.unab.dunab.domain.entity.Encuentro;
import com.unab.dunab.domain.entity.Inscripcion;
import com.unab.dunab.domain.entity.Logro;
import com.unab.dunab.domain.entity.Recompensa;
import com.unab.dunab.domain.entity.Usuario;
import com.unab.dunab.domain.enums.EstadoEncuentro;
import com.unab.dunab.domain.enums.TipoTransaccion;
import com.unab.dunab.repository.EncuentroRepository;
import com.unab.dunab.repository.InscripcionRepository;
import com.unab.dunab.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio central de encuentros: CRUD, inscripción, cancelación,
 * registro de asistencia y aplicación de penalizaciones.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EncuentroService {

    private final EncuentroRepository encuentroRepository;
    private final InscripcionRepository inscripcionRepository;
    private final UsuarioRepository usuarioRepository;
    private final TransaccionService transaccionService;
    private final LogroService logroService;
    private final FeedService feedService;

    /* ─── Consulta pública (estudiantes) ─────────────────── */

    @Transactional(readOnly = true)
    public List<EncuentroResponse> listarVisibles(String correoUsuario,
                                                   String lugar,
                                                   EstadoEncuentro estado,
                                                   LocalDate desde,
                                                   LocalDate hasta) {
        Long userId = resolveUserId(correoUsuario);
        return encuentroRepository
                .buscarFiltrado(lugar, estado, desde, hasta)
                .stream()
                .map(e -> toResponse(e, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EncuentroResponse obtenerPorId(Long id, String correoUsuario) {
        Encuentro e = findEncuentro(id);
        Long userId = resolveUserId(correoUsuario);
        return toResponse(e, userId);
    }

    /* ─── CRUD (admin) ────────────────────────────────────── */

    @Transactional(readOnly = true)
    public List<EncuentroResponse> listarTodos() {
        return encuentroRepository.findAll()
                .stream()
                .map(e -> toResponse(e, null))
                .collect(Collectors.toList());
    }

    @Transactional
    public EncuentroResponse crear(EncuentroRequest req, String correoAdmin) {
        Usuario admin = usuarioRepository.findByCorreo(correoAdmin)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));

        Encuentro e = Encuentro.builder()
                .nombre(req.getNombre())
                .descripcion(req.getDescripcion())
                .lugar(req.getLugar())
                .fecha(req.getFecha())
                .horaInicio(req.getHoraInicio())
                .horaFin(req.getHoraFin())
                .dunabRecompensa(req.getDunabRecompensa())
                .dunabPenalizacion(req.getDunabPenalizacion() != null ? req.getDunabPenalizacion() : 0)
                .cuposMax(req.getCuposMax())
                .estado(req.getEstado() != null ? req.getEstado() : EstadoEncuentro.ACTIVO)
                .visible(req.getVisible() != null ? req.getVisible() : true)
                .creadoPor(admin)
                .build();

        return toResponse(encuentroRepository.save(e), null);
    }

    @Transactional
    public EncuentroResponse actualizar(Long id, EncuentroRequest req) {
        Encuentro e = findEncuentro(id);

        e.setNombre(req.getNombre());
        e.setDescripcion(req.getDescripcion());
        e.setLugar(req.getLugar());
        e.setFecha(req.getFecha());
        e.setHoraInicio(req.getHoraInicio());
        e.setHoraFin(req.getHoraFin());
        e.setDunabRecompensa(req.getDunabRecompensa());
        if (req.getDunabPenalizacion() != null) e.setDunabPenalizacion(req.getDunabPenalizacion());
        e.setCuposMax(req.getCuposMax());
        if (req.getEstado()  != null) e.setEstado(req.getEstado());
        if (req.getVisible() != null) e.setVisible(req.getVisible());

        return toResponse(encuentroRepository.save(e), null);
    }

    @Transactional
    public void eliminar(Long id) {
        Encuentro e = findEncuentro(id);
        encuentroRepository.delete(e);
    }

    @Transactional
    public EncuentroResponse toggleVisibilidad(Long id) {
        Encuentro e = findEncuentro(id);
        e.setVisible(!e.getVisible());
        return toResponse(encuentroRepository.save(e), null);
    }

    /* ─── Inscripción ─────────────────────────────────────── */

    @Transactional
    public void inscribir(Long encuentroId, String correoUsuario) {
        Encuentro e = findEncuentro(encuentroId);
        Usuario u   = findUsuario(correoUsuario);

        if (e.getEstado() == EstadoEncuentro.INACTIVO || !e.getVisible()) {
            throw DunabException.badRequest("El encuentro no está disponible para inscripción.");
        }
        if (!e.tieneCuposDisponibles()) {
            throw DunabException.conflict("El encuentro no tiene cupos disponibles.");
        }
        if (inscripcionRepository.existsByUsuarioIdAndEncuentroId(u.getId(), encuentroId)) {
            throw DunabException.conflict("Ya estás inscrito en este encuentro.");
        }

        e.incrementarCupos();
        encuentroRepository.save(e);

        Inscripcion ins = Inscripcion.builder()
                .usuario(u)
                .encuentro(e)
                .build();
        inscripcionRepository.save(ins);

        // Publicar en el feed de actividad
        feedService.publicarInscripcion(u, e);
    }

    @Transactional
    public void cancelarInscripcion(Long encuentroId, String correoUsuario) {
        Usuario u = findUsuario(correoUsuario);
        Inscripcion ins = inscripcionRepository
                .findByUsuarioIdAndEncuentroId(u.getId(), encuentroId)
                .orElseThrow(() -> DunabException.notFound("No estás inscrito en este encuentro."));

        Encuentro e = ins.getEncuentro();
        e.decrementarCupos();
        encuentroRepository.save(e);

        inscripcionRepository.delete(ins);
    }

    /* ─── Asistencia y penalización ───────────────────────── */

    /**
     * Registra asistencia de un estudiante a un encuentro.
     * Acredita DUNAB de recompensa y evalúa logros.
     */
    @Transactional
    public void registrarAsistencia(Long encuentroId, Long usuarioId, boolean asistio) {
        Inscripcion ins = inscripcionRepository
                .findByUsuarioIdAndEncuentroId(usuarioId, encuentroId)
                .orElseThrow(() -> DunabException.notFound("Inscripción no encontrada."));

        ins.setAsistio(asistio);
        Encuentro e = ins.getEncuentro();
        Usuario u   = ins.getUsuario();

        if (asistio) {
            u.acreditarDunab(e.getDunabRecompensa());
            transaccionService.registrar(u, TipoTransaccion.INGRESO,
                    e.getDunabRecompensa(),
                    "Asistencia al encuentro: " + e.getNombre(), e.getId());
            // Evaluar logros y publicar en feed si se desbloquean nuevos
            List<Logro> nuevosLogros = logroService.evaluarLogros(u);
            nuevosLogros.forEach(logro -> feedService.publicarLogro(u, logro));
            // Evaluar si alcanzó un hito de saldo
            feedService.evaluarYPublicarHito(u);
            usuarioRepository.save(u);
        }

        inscripcionRepository.save(ins);
    }

    /**
     * Aplica penalización a todos los inscritos que no asistieron
     * y cuya inscripción no haya sido penalizada aún.
     */
    @Transactional
    public int aplicarPenalizaciones(Long encuentroId) {
        Encuentro e = findEncuentro(encuentroId);
        List<Inscripcion> pendientes = inscripcionRepository
                .findByEncuentroId(encuentroId)
                .stream()
                .filter(i -> Boolean.FALSE.equals(i.getAsistio()) && !i.getPenalizado())
                .collect(Collectors.toList());

        pendientes.forEach(ins -> {
            Usuario u = ins.getUsuario();
            int pen = e.getDunabPenalizacion();
            if (pen > 0 && u.tieneSaldo(pen)) {
                u.debitarDunab(pen);
                transaccionService.registrar(u, TipoTransaccion.PENALIZACION,
                        pen, "Penalización por inasistencia: " + e.getNombre(), e.getId());
                usuarioRepository.save(u);
            }
            ins.setPenalizado(true);
            inscripcionRepository.save(ins);
        });

        return pendientes.size();
    }

    /* ─── Inscritos por encuentro ────────────────────────── */

    @Transactional(readOnly = true)
    public List<InscripcionDetalleResponse> listarInscritos(Long encuentroId) {
        findEncuentro(encuentroId); // valida que exista
        return inscripcionRepository.findByEncuentroId(encuentroId)
                .stream()
                .map(ins -> InscripcionDetalleResponse.builder()
                        .inscripcionId(ins.getId())
                        .usuarioId(ins.getUsuario().getId())
                        .nombre(ins.getUsuario().getNombre())
                        .correo(ins.getUsuario().getCorreo())
                        .codigo(ins.getUsuario().getCodigo())
                        .asistio(ins.getAsistio())
                        .penalizado(ins.getPenalizado())
                        .inscritoEn(ins.getInscritoEn())
                        .build())
                .collect(Collectors.toList());
    }

    /* ─── Helpers ─────────────────────────────────────────── */

    private Encuentro findEncuentro(Long id) {
        return encuentroRepository.findById(id)
                .orElseThrow(() -> DunabException.notFound("Encuentro no encontrado: " + id));
    }

    private Usuario findUsuario(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));
    }

    private Long resolveUserId(String correo) {
        if (correo == null) return null;
        return usuarioRepository.findByCorreo(correo).map(Usuario::getId).orElse(null);
    }

    public EncuentroResponse toResponse(Encuentro e, Long userId) {
        boolean inscrito = userId != null &&
                inscripcionRepository.existsByUsuarioIdAndEncuentroId(userId, e.getId());
        return EncuentroResponse.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .descripcion(e.getDescripcion())
                .lugar(e.getLugar())
                .fecha(e.getFecha())
                .horaInicio(e.getHoraInicio())
                .horaFin(e.getHoraFin())
                .dunabRecompensa(e.getDunabRecompensa())
                .dunabPenalizacion(e.getDunabPenalizacion())
                .cuposMax(e.getCuposMax())
                .cuposOcupados(e.getCuposOcupados())
                .cuposLibres(e.getCuposLibres())
                .estado(e.getEstado().name())
                .visible(e.getVisible())
                .inscrito(inscrito)
                .build();
    }
}
