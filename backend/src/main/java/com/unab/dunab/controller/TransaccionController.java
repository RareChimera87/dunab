package com.unab.dunab.controller;

import com.unab.dunab.dto.response.TransaccionResponse;
import com.unab.dunab.service.TransaccionService;
import com.unab.dunab.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Endpoints del historial de transacciones y estadísticas.
 *
 *   GET /api/transacciones/me         → Historial del usuario autenticado
 *   GET /api/estadisticas/me          → Estadísticas de DUNAB (promedios por período)
 */
@Tag(name = "Transacciones")
@RestController
@RequiredArgsConstructor
public class TransaccionController {

    private final TransaccionService transaccionService;
    private final UsuarioService usuarioService;

    @Operation(summary = "Historial de transacciones del usuario autenticado")
    @GetMapping("/transacciones/me")
    public ResponseEntity<List<TransaccionResponse>> historial(Principal principal) {
        Long userId = usuarioService.obtenerPorCorreo(principal.getName()).getId();
        return ResponseEntity.ok(transaccionService.obtenerHistorial(userId));
    }

    @Operation(summary = "Estadísticas de DUNAB: promedios por semana, mes, semestre y año")
    @GetMapping("/estadisticas/me")
    public ResponseEntity<Map<String, Object>> estadisticas(Principal principal) {
        Long userId = usuarioService.obtenerPorCorreo(principal.getName()).getId();
        return ResponseEntity.ok(transaccionService.obtenerEstadisticas(userId));
    }
}
