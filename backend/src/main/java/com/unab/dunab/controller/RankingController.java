package com.unab.dunab.controller;

import com.unab.dunab.dto.response.RankingResponse;
import com.unab.dunab.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Endpoints del ranking de estudiantes.
 *
 *   GET /api/ranking               → Ranking completo (activos, desc balance)
 *   GET /api/ranking/top?limite=N  → Top N estudiantes
 *   GET /api/ranking/mi-posicion   → Posición y porcentaje del usuario autenticado
 *   POST /api/ranking/admin/evaluar-logros → [Admin] Re-evaluar logros de ranking
 */
@Tag(name = "Ranking")
@RestController
@RequestMapping("/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @Operation(summary = "Ranking completo de estudiantes")
    @GetMapping
    public ResponseEntity<List<RankingResponse>> ranking() {
        return ResponseEntity.ok(rankingService.obtenerRanking());
    }

    @Operation(summary = "Top N estudiantes del ranking")
    @GetMapping("/top")
    public ResponseEntity<List<RankingResponse>> top(
            @RequestParam(defaultValue = "10") int limite) {
        if (limite < 1 || limite > 100) limite = 10;
        return ResponseEntity.ok(rankingService.obtenerTop(limite));
    }

    @Operation(summary = "Mi posición actual en el ranking")
    @GetMapping("/mi-posicion")
    public ResponseEntity<Map<String, Object>> miPosicion(Principal principal) {
        return ResponseEntity.ok(rankingService.miPosicion(principal.getName()));
    }

    @Operation(summary = "[Admin] Re-evaluar logros de tipo RANKING para todos")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/evaluar-logros")
    public ResponseEntity<Map<String, String>> evaluarLogros() {
        rankingService.evaluarLogrosRanking();
        return ResponseEntity.ok(Map.of("mensaje", "Logros de ranking evaluados correctamente."));
    }
}
