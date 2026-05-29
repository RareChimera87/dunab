package com.unab.dunab.service;

import com.unab.dunab.dto.request.TransferenciaRequest;
import com.unab.dunab.common.exception.DunabException;
import com.unab.dunab.domain.enums.TipoTransaccion;
import com.unab.dunab.domain.entity.Transferencia;
import com.unab.dunab.domain.entity.Usuario;
import com.unab.dunab.repository.TransferenciaRepository;
import com.unab.dunab.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de transferencias P2P de DUNAB entre estudiantes.
 * Límite: 1–2000 DUNAB por operación.
 */
@Service
@RequiredArgsConstructor
public class TransferenciaService {

    private final TransferenciaRepository transferenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TransaccionService transaccionService;
    private final LogroService logroService;
    private final FeedService feedService;

    /* ─── Enviar transferencia ────────────────────────────── */

    @Transactional
    public Map<String, Object> enviar(String correoRemitente, TransferenciaRequest req) {
        Usuario remitente = usuarioRepository.findByCorreo(correoRemitente)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));

        Usuario destinatario = usuarioRepository.findById(req.getDestinatarioId())
                .orElseThrow(() -> DunabException.notFound("Destinatario no encontrado"));

        if (remitente.getId().equals(destinatario.getId())) {
            throw DunabException.badRequest("No puedes transferirte DUNAB a ti mismo.");
        }
        if (!destinatario.getActivo()) {
            throw DunabException.badRequest("El destinatario no tiene una cuenta activa.");
        }
        if (!remitente.tieneSaldo(req.getMonto())) {
            throw DunabException.badRequest("Saldo insuficiente de DUNAB.");
        }

        // Mover DUNAB
        remitente.debitarDunab(req.getMonto());
        destinatario.acreditarDunab(req.getMonto());

        usuarioRepository.save(remitente);
        usuarioRepository.save(destinatario);

        // Guardar transferencia
        Transferencia tx = Transferencia.builder()
                .remitente(remitente)
                .destinatario(destinatario)
                .monto(req.getMonto())
                .nota(req.getNota())
                .estado("COMPLETADA")
                .build();
        Transferencia saved = transferenciaRepository.save(tx);

        // Registrar transacciones en historial
        String notaTexto = req.getNota() != null && !req.getNota().isBlank()
                ? " · Nota: " + req.getNota() : "";

        transaccionService.registrar(remitente, TipoTransaccion.TRANSFERENCIA_ENVIADA,
                req.getMonto(),
                "Transferencia enviada a " + destinatario.getNombre() + notaTexto,
                saved.getId());

        transaccionService.registrar(destinatario, TipoTransaccion.TRANSFERENCIA_RECIBIDA,
                req.getMonto(),
                "Transferencia recibida de " + remitente.getNombre() + notaTexto,
                saved.getId());

        // Evaluar logros y hitos tras la transferencia
        logroService.evaluarLogros(remitente);
        feedService.evaluarYPublicarHito(destinatario);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("transferenciaId", saved.getId());
        result.put("monto",          req.getMonto());
        result.put("destinatario",   destinatario.getNombre());
        result.put("balanceRemitente", remitente.getBalanceDunab());
        result.put("estado",         "COMPLETADA");
        result.put("fecha",          saved.getCreadoEn().toString());
        return result;
    }

    /* ─── Historial ───────────────────────────────────────── */

    @Transactional(readOnly = true)
    public List<Map<String, Object>> historialEnviadas(String correo) {
        Usuario u = findByCorreo(correo);
        return transferenciaRepository
                .findByRemitenteIdOrderByCreadoEnDesc(u.getId())
                .stream()
                .map(t -> toMap(t, "ENVIADA"))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> historialRecibidas(String correo) {
        Usuario u = findByCorreo(correo);
        return transferenciaRepository
                .findByDestinatarioIdOrderByCreadoEnDesc(u.getId())
                .stream()
                .map(t -> toMap(t, "RECIBIDA"))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> historialCompleto(String correo) {
        Usuario u = findByCorreo(correo);
        Long uid = u.getId();

        List<Map<String, Object>> enviadas  = transferenciaRepository
                .findByRemitenteIdOrderByCreadoEnDesc(uid)
                .stream().map(t -> toMap(t, "ENVIADA")).collect(Collectors.toList());

        List<Map<String, Object>> recibidas = transferenciaRepository
                .findByDestinatarioIdOrderByCreadoEnDesc(uid)
                .stream().map(t -> toMap(t, "RECIBIDA")).collect(Collectors.toList());

        enviadas.addAll(recibidas);
        enviadas.sort((a, b) -> b.get("fecha").toString().compareTo(a.get("fecha").toString()));
        return enviadas;
    }

    /* ─── Búsqueda de destinatario ───────────────────────── */

    @Transactional(readOnly = true)
    public List<Map<String, Object>> buscarDestinatarios(String correoRemitente, String query) {
        if (query == null || query.trim().length() < 2) return List.of();
        Usuario yo = findByCorreo(correoRemitente);

        return usuarioRepository.findAll()
                .stream()
                .filter(u -> u.getActivo() && !u.getId().equals(yo.getId()))
                .filter(u -> u.getNombre().toLowerCase().contains(query.toLowerCase())
                          || u.getCodigo().toLowerCase().contains(query.toLowerCase())
                          || u.getCorreo().toLowerCase().contains(query.toLowerCase()))
                .limit(10)
                .map(u -> Map.<String, Object>of(
                        "id",     u.getId(),
                        "nombre", u.getNombre(),
                        "codigo", u.getCodigo(),
                        "correo", u.getCorreo()
                ))
                .collect(Collectors.toList());
    }

    /* ─── Helpers ─────────────────────────────────────────── */

    private Usuario findByCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));
    }

    private Map<String, Object> toMap(Transferencia t, String direccion) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",            t.getId());
        m.put("direccion",     direccion);
        m.put("monto",         t.getMonto());
        m.put("nota",          t.getNota() != null ? t.getNota() : "");
        m.put("estado",        t.getEstado());
        m.put("fecha",         t.getCreadoEn().toString());
        m.put("remitenteId",   t.getRemitente().getId());
        m.put("remitente",     t.getRemitente().getNombre());
        m.put("destinatarioId",t.getDestinatario().getId());
        m.put("destinatario",  t.getDestinatario().getNombre());
        return m;
    }
}
