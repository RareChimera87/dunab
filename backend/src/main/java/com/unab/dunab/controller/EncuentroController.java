package com.unab.dunab.controller;

import com.unab.dunab.dto.request.EncuentroRequest;
import com.unab.dunab.dto.response.EncuentroResponse;
import com.unab.dunab.dto.response.InscripcionDetalleResponse;
import com.unab.dunab.domain.enums.EstadoEncuentro;
import com.unab.dunab.service.EncuentroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Endpoints de Encuentros.
 *
 * Estudiante:
 *   GET  /api/encuentros               → Listar encuentros visibles (con filtros)
 *   GET  /api/encuentros/{id}          → Detalle de un encuentro
 *
 * Admin / Superadmin:
 *   GET  /api/encuentros/admin/todos          → Listar todos (incluye ocultos)
 *   GET  /api/encuentros/admin/{id}/inscritos → Lista de inscritos con asistencia
 *   POST /api/encuentros/admin                → Crear encuentro
 *   PUT  /api/encuentros/admin/{id}           → Editar encuentro
 *   DEL  /api/encuentros/admin/{id}           → Eliminar encuentro
 *   PUT  /api/encuentros/admin/{id}/visibilidad → Toggle visible
 *   POST /api/encuentros/admin/{id}/penalizar   → Aplicar penalizaciones
 */
@Tag(name = "Encuentros")
@RestController
@RequestMapping("/encuentros")
@RequiredArgsConstructor
public class EncuentroController {

    private final EncuentroService encuentroService;

    /* ─── Estudiante ─────────────────────────────────────── */

    @Operation(summary = "Listar encuentros disponibles (con filtros opcionales)")
    @GetMapping
    public ResponseEntity<List<EncuentroResponse>> listar(
            Principal principal,
            @RequestParam(required = false) String lugar,
            @RequestParam(required = false) EstadoEncuentro estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        String correo = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(encuentroService.listarVisibles(correo, lugar, estado, desde, hasta));
    }

    @Operation(summary = "Detalle de un encuentro")
    @GetMapping("/{id}")
    public ResponseEntity<EncuentroResponse> detalle(@PathVariable Long id, Principal principal) {
        String correo = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(encuentroService.obtenerPorId(id, correo));
    }

    /* ─── Admin / Superadmin ─────────────────────────────── */

    @Operation(summary = "[Admin] Listar todos los encuentros")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @GetMapping("/admin/todos")
    public ResponseEntity<List<EncuentroResponse>> listarTodos() {
        return ResponseEntity.ok(encuentroService.listarTodos());
    }

    @Operation(summary = "[Admin] Listar inscritos de un encuentro con estado de asistencia")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @GetMapping("/admin/{id}/inscritos")
    public ResponseEntity<List<InscripcionDetalleResponse>> listarInscritos(@PathVariable Long id) {
        return ResponseEntity.ok(encuentroService.listarInscritos(id));
    }

    @Operation(summary = "[Admin] Crear nuevo encuentro")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @PostMapping("/admin")
    public ResponseEntity<EncuentroResponse> crear(
            @Valid @RequestBody EncuentroRequest req,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(encuentroService.crear(req, principal.getName()));
    }

    @Operation(summary = "[Admin] Editar encuentro")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @PutMapping("/admin/{id}")
    public ResponseEntity<EncuentroResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EncuentroRequest req) {
        return ResponseEntity.ok(encuentroService.actualizar(id, req));
    }

    @Operation(summary = "[Admin] Eliminar encuentro")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long id) {
        encuentroService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Encuentro eliminado correctamente."));
    }

    @Operation(summary = "[Admin] Toggle visibilidad del encuentro")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @PutMapping("/admin/{id}/visibilidad")
    public ResponseEntity<EncuentroResponse> toggleVisibilidad(@PathVariable Long id) {
        return ResponseEntity.ok(encuentroService.toggleVisibilidad(id));
    }

    @Operation(summary = "[Admin] Aplicar penalizaciones por inasistencia")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @PostMapping("/admin/{id}/penalizar")
    public ResponseEntity<Map<String, Object>> penalizar(@PathVariable Long id) {
        int afectados = encuentroService.aplicarPenalizaciones(id);
        return ResponseEntity.ok(Map.of(
                "mensaje",   "Penalizaciones aplicadas.",
                "afectados", afectados
        ));
    }
}
