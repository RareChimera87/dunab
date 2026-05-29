package com.unab.dunab.controller;

import com.unab.dunab.service.EncuentroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

/**
 * Endpoints de inscripción/cancelación en encuentros.
 *
 *   POST /api/inscripciones/{encuentroId}       → Inscribirse
 *   DEL  /api/inscripciones/{encuentroId}       → Cancelar inscripción
 *
 * Admin:
 *   POST /api/inscripciones/admin/{encuentroId}/asistencia → Registrar asistencia
 */
@Tag(name = "Inscripciones")
@RestController
@RequestMapping("/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {

    private final EncuentroService encuentroService;

    @Operation(summary = "Inscribirse en un encuentro")
    @PostMapping("/{encuentroId}")
    public ResponseEntity<Map<String, String>> inscribir(
            @PathVariable Long encuentroId,
            Principal principal) {
        encuentroService.inscribir(encuentroId, principal.getName());
        return ResponseEntity.ok(Map.of("mensaje", "Inscripción realizada correctamente."));
    }

    @Operation(summary = "Cancelar inscripción en un encuentro")
    @DeleteMapping("/{encuentroId}")
    public ResponseEntity<Map<String, String>> cancelar(
            @PathVariable Long encuentroId,
            Principal principal) {
        encuentroService.cancelarInscripcion(encuentroId, principal.getName());
        return ResponseEntity.ok(Map.of("mensaje", "Inscripción cancelada correctamente."));
    }

    @Operation(summary = "[Admin] Registrar asistencia de un usuario")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @PostMapping("/admin/{encuentroId}/asistencia")
    public ResponseEntity<Map<String, String>> registrarAsistencia(
            @PathVariable Long encuentroId,
            @RequestBody Map<String, Object> body) {
        Long usuarioId = Long.valueOf(body.get("usuarioId").toString());
        boolean asistio = Boolean.parseBoolean(body.get("asistio").toString());
        encuentroService.registrarAsistencia(encuentroId, usuarioId, asistio);
        return ResponseEntity.ok(Map.of("mensaje", "Asistencia registrada correctamente."));
    }
}
