package com.unab.dunab.controller;

import com.unab.dunab.dto.request.FeedPublicacionRequest;
import com.unab.dunab.dto.response.FeedEventResponse;
import com.unab.dunab.domain.enums.FeedTipo;
import com.unab.dunab.service.FeedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Endpoints del feed de actividad global.
 *
 * <pre>
 * GET  /api/feed              → feed global paginado (opcional: ?tipo=LOGRO&page=0)
 * GET  /api/feed/mini         → últimos 6 eventos para el dashboard
 * POST /api/feed/publicar     → nueva publicación del usuario autenticado
 * DELETE /api/feed/{id}       → eliminar publicación propia (o ADMIN)
 * </pre>
 */
@RestController
@RequestMapping("/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    /**
     * Feed global paginado.
     * Query params opcionales:
     * - {@code tipo}: INSCRIPCION | LOGRO | HITO | PUBLICACION
     * - {@code page}: número de página (0-based, default 0)
     */
    @GetMapping
    public ResponseEntity<Page<FeedEventResponse>> getFeed(
            @RequestParam(required = false) FeedTipo tipo,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(feedService.obtenerFeed(tipo, page));
    }

    /**
     * Mini-feed: últimos 6 eventos para el widget del dashboard.
     * No requiere parámetros.
     */
    @GetMapping("/mini")
    public ResponseEntity<List<FeedEventResponse>> getMiniFeed() {
        return ResponseEntity.ok(feedService.obtenerMiniFeed());
    }

    /**
     * Crear una publicación libre en el feed.
     */
    @PostMapping("/publicar")
    public ResponseEntity<FeedEventResponse> publicar(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody FeedPublicacionRequest req) {
        FeedEventResponse resp = feedService.publicar(userDetails.getUsername(), req);
        return ResponseEntity.ok(resp);
    }

    /**
     * Eliminar una publicación (soft-delete).
     * Solo el autor o un ADMIN puede eliminar.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        feedService.eliminar(id, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("mensaje", "Publicación eliminada."));
    }
}
