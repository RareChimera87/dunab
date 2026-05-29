package com.unab.dunab.controller;

import com.unab.dunab.dto.request.RecompensaRequest;
import com.unab.dunab.dto.response.CanjeResponse;
import com.unab.dunab.dto.response.RecompensaResponse;
import com.unab.dunab.service.RecompensaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Endpoints de la Tienda de Recompensas.
 *
 * <pre>
 * — Estudiante —
 * GET  /recompensas                → catálogo de recompensas activas
 * POST /recompensas/{id}/canjear   → canjear una recompensa
 * GET  /recompensas/mis-canjes     → historial de canjes propios
 *
 * — Admin —
 * GET    /recompensas/admin/todas          → todas las recompensas (activas + inactivas)
 * POST   /recompensas/admin                → crear recompensa
 * PUT    /recompensas/admin/{id}           → editar recompensa
 * DELETE /recompensas/admin/{id}           → eliminar recompensa
 * PATCH  /recompensas/admin/{id}/toggle    → activar/desactivar
 * GET    /recompensas/admin/canjes         → todos los canjes
 * PATCH  /recompensas/admin/canjes/{id}/entregar  → marcar como entregado
 * PATCH  /recompensas/admin/canjes/{id}/cancelar  → cancelar y devolver DUNAB
 * GET    /recompensas/admin/canjes/buscar  → buscar canje por código
 * GET    /recompensas/admin/canjes/pendientes-count → cantidad de canjes pendientes
 * </pre>
 */
@RestController
@RequestMapping("/recompensas")
@RequiredArgsConstructor
public class RecompensaController {

    private final RecompensaService recompensaService;

    /* ══════════════════ ENDPOINTS ESTUDIANTE ══════════════════ */

    @GetMapping
    public ResponseEntity<List<RecompensaResponse>> getCatalogo() {
        return ResponseEntity.ok(recompensaService.listarActivas());
    }

    @PostMapping("/{id}/canjear")
    public ResponseEntity<CanjeResponse> canjear(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(recompensaService.canjear(id, userDetails.getUsername()));
    }

    @GetMapping("/mis-canjes")
    public ResponseEntity<List<CanjeResponse>> misCanjes(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(recompensaService.misCanjes(userDetails.getUsername()));
    }

    /* ══════════════════ ENDPOINTS ADMIN ══════════════════ */

    @GetMapping("/admin/todas")
    public ResponseEntity<List<RecompensaResponse>> getTodas() {
        return ResponseEntity.ok(recompensaService.listarTodas());
    }

    @PostMapping("/admin")
    public ResponseEntity<RecompensaResponse> crear(
            @Valid @RequestBody RecompensaRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(recompensaService.crear(req, userDetails.getUsername()));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<RecompensaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody RecompensaRequest req) {
        return ResponseEntity.ok(recompensaService.actualizar(id, req));
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long id) {
        recompensaService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Recompensa eliminada."));
    }

    @PatchMapping("/admin/{id}/toggle")
    public ResponseEntity<RecompensaResponse> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(recompensaService.toggleActiva(id));
    }

    @GetMapping("/admin/canjes")
    public ResponseEntity<List<CanjeResponse>> todosLosCanjes() {
        return ResponseEntity.ok(recompensaService.todosLosCanjes());
    }

    @PatchMapping("/admin/canjes/{id}/entregar")
    public ResponseEntity<CanjeResponse> marcarEntregado(@PathVariable Long id) {
        return ResponseEntity.ok(recompensaService.marcarEntregado(id));
    }

    @PatchMapping("/admin/canjes/{id}/cancelar")
    public ResponseEntity<CanjeResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(recompensaService.cancelarCanje(id));
    }

    @GetMapping("/admin/canjes/buscar")
    public ResponseEntity<CanjeResponse> buscarCodigo(@RequestParam String codigo) {
        return ResponseEntity.ok(recompensaService.buscarPorCodigo(codigo));
    }

    @GetMapping("/admin/canjes/pendientes-count")
    public ResponseEntity<Map<String, Long>> pendientesCount() {
        return ResponseEntity.ok(Map.of("pendientes", recompensaService.canjesPendientes()));
    }
}
