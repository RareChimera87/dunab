package com.unab.dunab.service;

import com.unab.dunab.dto.request.RecompensaRequest;
import com.unab.dunab.dto.response.CanjeResponse;
import com.unab.dunab.dto.response.RecompensaResponse;
import com.unab.dunab.common.exception.DunabException;
import com.unab.dunab.domain.entity.Canje;
import com.unab.dunab.domain.entity.Recompensa;
import com.unab.dunab.domain.entity.Usuario;
import com.unab.dunab.domain.enums.TipoTransaccion;
import com.unab.dunab.repository.CanjeRepository;
import com.unab.dunab.repository.RecompensaRepository;
import com.unab.dunab.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de la Tienda de Recompensas DUNAB.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>CRUD de recompensas (solo admin).</li>
 *   <li>Canje de recompensas por parte de estudiantes.</li>
 *   <li>Gestión de canjes (marcar como entregado, cancelar).</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecompensaService {

    private final RecompensaRepository  recompensaRepository;
    private final CanjeRepository       canjeRepository;
    private final UsuarioRepository     usuarioRepository;
    private final TransaccionService    transaccionService;

    /* ── Catálogo ─────────────────────────────────────────────────────────── */

    /** Lista recompensas activas (vista estudiante). */
    @Transactional(readOnly = true)
    public List<RecompensaResponse> listarActivas() {
        return recompensaRepository.findActivas()
                .stream().map(RecompensaResponse::from).toList();
    }

    /** Lista todas las recompensas (vista admin). */
    @Transactional(readOnly = true)
    public List<RecompensaResponse> listarTodas() {
        return recompensaRepository.findAllOrdenadas()
                .stream().map(RecompensaResponse::from).toList();
    }

    /** Crea una nueva recompensa. */
    @Transactional
    public RecompensaResponse crear(RecompensaRequest req, String correoAdmin) {
        Usuario admin = usuarioRepository.findByCorreo(correoAdmin)
                .orElseThrow(() -> DunabException.notFound("Admin no encontrado"));

        Recompensa r = Recompensa.builder()
                .nombre(req.getNombre().trim())
                .descripcion(req.getDescripcion() != null ? req.getDescripcion().trim() : null)
                .emoji(req.getEmoji() != null && !req.getEmoji().isBlank() ? req.getEmoji() : "🎁")
                .categoria(req.getCategoria() != null && !req.getCategoria().isBlank()
                        ? req.getCategoria().trim().toUpperCase() : "GENERAL")
                .costoDunab(req.getCostoDunab())
                .stock(req.getStock() != null ? req.getStock() : -1)
                .activa(req.getActiva() != null ? req.getActiva() : true)
                .creadoPor(admin)
                .build();

        return RecompensaResponse.from(recompensaRepository.save(r));
    }

    /** Actualiza una recompensa existente. */
    @Transactional
    public RecompensaResponse actualizar(Long id, RecompensaRequest req) {
        Recompensa r = recompensaRepository.findById(id)
                .orElseThrow(() -> DunabException.notFound("Recompensa no encontrada"));

        r.setNombre(req.getNombre().trim());
        r.setDescripcion(req.getDescripcion() != null ? req.getDescripcion().trim() : null);
        if (req.getEmoji() != null && !req.getEmoji().isBlank()) r.setEmoji(req.getEmoji());
        if (req.getCategoria() != null && !req.getCategoria().isBlank())
            r.setCategoria(req.getCategoria().trim().toUpperCase());
        r.setCostoDunab(req.getCostoDunab());
        if (req.getStock() != null) r.setStock(req.getStock());
        if (req.getActiva() != null) r.setActiva(req.getActiva());
        r.setActualizadoEn(LocalDateTime.now());

        return RecompensaResponse.from(recompensaRepository.save(r));
    }

    /** Elimina una recompensa (solo si no tiene canjes asociados). */
    @Transactional
    public void eliminar(Long id) {
        Recompensa r = recompensaRepository.findById(id)
                .orElseThrow(() -> DunabException.notFound("Recompensa no encontrada"));
        recompensaRepository.delete(r);
    }

    /** Activa o desactiva una recompensa. */
    @Transactional
    public RecompensaResponse toggleActiva(Long id) {
        Recompensa r = recompensaRepository.findById(id)
                .orElseThrow(() -> DunabException.notFound("Recompensa no encontrada"));
        r.setActiva(!r.getActiva());
        r.setActualizadoEn(LocalDateTime.now());
        return RecompensaResponse.from(recompensaRepository.save(r));
    }

    /* ── Canjes ───────────────────────────────────────────────────────────── */

    /**
     * Canjea una recompensa: descuenta DUNAB al estudiante y genera un código de reclamación.
     */
    @Transactional
    public CanjeResponse canjear(Long recompensaId, String correoEstudiante) {
        Usuario estudiante = usuarioRepository.findByCorreo(correoEstudiante)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));

        Recompensa recompensa = recompensaRepository.findById(recompensaId)
                .orElseThrow(() -> DunabException.notFound("Recompensa no encontrada"));

        if (!recompensa.getActiva()) {
            throw DunabException.badRequest("Esta recompensa no está disponible.");
        }
        if (!recompensa.hayStock()) {
            throw DunabException.badRequest("Esta recompensa está agotada.");
        }
        if (estudiante.getBalanceDunab() < recompensa.getCostoDunab()) {
            throw DunabException.badRequest(
                    "Balance insuficiente. Necesitas " + recompensa.getCostoDunab()
                    + " DUNAB y tienes " + estudiante.getBalanceDunab() + ".");
        }

        // Descontar DUNAB
        estudiante.setBalanceDunab(estudiante.getBalanceDunab() - recompensa.getCostoDunab());
        usuarioRepository.save(estudiante);

        // Registrar transacción de egreso
        transaccionService.registrar(
                estudiante,
                TipoTransaccion.EGRESO,
                -recompensa.getCostoDunab(),
                "Canje: " + recompensa.getNombre(),
                recompensaId
        );

        // Descontar stock si es limitado
        recompensa.descontarStock();
        recompensaRepository.save(recompensa);

        // Crear el canje con código único
        Canje canje = Canje.builder()
                .usuario(estudiante)
                .recompensa(recompensa)
                .costoDunab(recompensa.getCostoDunab())
                .codigoCanje(generarCodigo())
                .build();

        Canje guardado = canjeRepository.save(canje);
        log.info("Canje creado: {} por usuario {}", guardado.getCodigoCanje(), correoEstudiante);
        return CanjeResponse.from(guardado);
    }

    /** Historial de canjes del estudiante autenticado. */
    @Transactional(readOnly = true)
    public List<CanjeResponse> misCanjes(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));
        return canjeRepository.findByUsuario(usuario.getId())
                .stream().map(CanjeResponse::from).toList();
    }

    /** Todos los canjes (vista admin). */
    @Transactional(readOnly = true)
    public List<CanjeResponse> todosLosCanjes() {
        return canjeRepository.findAllOrdenados()
                .stream().map(CanjeResponse::from).toList();
    }

    /** Marca un canje como ENTREGADO. */
    @Transactional
    public CanjeResponse marcarEntregado(Long canjeId) {
        Canje canje = canjeRepository.findById(canjeId)
                .orElseThrow(() -> DunabException.notFound("Canje no encontrado"));
        if (!"PENDIENTE".equals(canje.getEstado())) {
            throw DunabException.badRequest("Solo se pueden entregar canjes en estado PENDIENTE.");
        }
        canje.setEstado("ENTREGADO");
        canje.setEntregadoEn(LocalDateTime.now());
        return CanjeResponse.from(canjeRepository.save(canje));
    }

    /** Cancela un canje y devuelve los DUNAB al estudiante. */
    @Transactional
    public CanjeResponse cancelarCanje(Long canjeId) {
        Canje canje = canjeRepository.findById(canjeId)
                .orElseThrow(() -> DunabException.notFound("Canje no encontrado"));
        if (!"PENDIENTE".equals(canje.getEstado())) {
            throw DunabException.badRequest("Solo se pueden cancelar canjes en estado PENDIENTE.");
        }
        // Devolver DUNAB
        Usuario usuario = canje.getUsuario();
        usuario.setBalanceDunab(usuario.getBalanceDunab() + canje.getCostoDunab());
        usuarioRepository.save(usuario);

        transaccionService.registrar(
                usuario,
                TipoTransaccion.INGRESO,
                canje.getCostoDunab(),
                "Devolución canje: " + canje.getRecompensa().getNombre(),
                canje.getId()
        );

        canje.setEstado("CANCELADO");
        return CanjeResponse.from(canjeRepository.save(canje));
    }

    /** Busca un canje por su código (para validación en físico). */
    @Transactional(readOnly = true)
    public CanjeResponse buscarPorCodigo(String codigo) {
        return canjeRepository.findByCodigoCanje(codigo.toUpperCase())
                .map(CanjeResponse::from)
                .orElseThrow(() -> DunabException.notFound("Código de canje no encontrado."));
    }

    /** Cantidad de canjes pendientes (para el badge del admin). */
    @Transactional(readOnly = true)
    public long canjesPendientes() {
        return canjeRepository.countByEstado("PENDIENTE");
    }

    /* ── Helpers ──────────────────────────────────────────────────────────── */

    private String generarCodigo() {
        String base = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "DUN-" + base;
    }
}
