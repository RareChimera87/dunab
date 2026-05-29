package com.unab.dunab.controller;

import com.unab.dunab.dto.request.TransferenciaRequest;
import com.unab.dunab.service.TransferenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Endpoints de transferencias P2P de DUNAB.
 *
 *   POST /api/transferencias           → Enviar DUNAB a otro estudiante
 *   GET  /api/transferencias/me        → Historial completo (enviadas + recibidas)
 *   GET  /api/transferencias/me/enviadas   → Solo enviadas
 *   GET  /api/transferencias/me/recibidas  → Solo recibidas
 *   GET  /api/transferencias/buscar?q= → Buscar destinatarios por nombre/código/correo
 */
@Tag(name = "Transferencias P2P")
@RestController
@RequestMapping("/transferencias")
@RequiredArgsConstructor
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    @Operation(summary = "Enviar DUNAB a otro estudiante (máx. 2000 por operación)")
    @PostMapping
    public ResponseEntity<Map<String, Object>> enviar(
            @Valid @RequestBody TransferenciaRequest req,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transferenciaService.enviar(principal.getName(), req));
    }

    @Operation(summary = "Historial completo de transferencias (enviadas y recibidas)")
    @GetMapping("/me")
    public ResponseEntity<List<Map<String, Object>>> historialCompleto(Principal principal) {
        return ResponseEntity.ok(transferenciaService.historialCompleto(principal.getName()));
    }

    @Operation(summary = "Solo transferencias enviadas")
    @GetMapping("/me/enviadas")
    public ResponseEntity<List<Map<String, Object>>> enviadas(Principal principal) {
        return ResponseEntity.ok(transferenciaService.historialEnviadas(principal.getName()));
    }

    @Operation(summary = "Solo transferencias recibidas")
    @GetMapping("/me/recibidas")
    public ResponseEntity<List<Map<String, Object>>> recibidas(Principal principal) {
        return ResponseEntity.ok(transferenciaService.historialRecibidas(principal.getName()));
    }

    @Operation(summary = "Buscar destinatarios por nombre, código o correo")
    @GetMapping("/buscar")
    public ResponseEntity<List<Map<String, Object>>> buscar(
            Principal principal,
            @RequestParam String q) {
        return ResponseEntity.ok(transferenciaService.buscarDestinatarios(principal.getName(), q));
    }
}
