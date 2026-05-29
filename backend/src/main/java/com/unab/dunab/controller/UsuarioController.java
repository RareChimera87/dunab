package com.unab.dunab.controller;

import com.unab.dunab.dto.request.RegisterRequest;
import com.unab.dunab.dto.request.UpdatePerfilRequest;
import com.unab.dunab.dto.response.UsuarioResponse;
import com.unab.dunab.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Endpoints de perfil y administración de usuarios.
 *
 * Estudiante:
 *   GET    /api/usuarios/me            → Mi perfil
 *   PUT    /api/usuarios/me            → Actualizar perfil
 *   PUT    /api/usuarios/me/contrasena → Cambiar contraseña
 *   GET    /api/usuarios/me/logros     → Mis logros/badges
 *
 * Admin:
 *   GET    /api/usuarios/admin/todos   → Listar todos los usuarios
 *   PUT    /api/usuarios/admin/{id}/toggle → Activar/desactivar cuenta
 *   GET    /api/usuarios/{id}          → Ver perfil de un usuario
 */
@Tag(name = "Usuarios")
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /* ─── Estudiante: mi perfil ─────────────────────────── */

    @Operation(summary = "Obtener mi perfil")
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> miPerfil(Principal principal) {
        return ResponseEntity.ok(usuarioService.obtenerPorCorreo(principal.getName()));
    }

    @Operation(summary = "Actualizar mi perfil")
    @PutMapping("/me")
    public ResponseEntity<UsuarioResponse> actualizarPerfil(
            Principal principal,
            @Valid @RequestBody UpdatePerfilRequest req) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(principal.getName(), req));
    }

    @Operation(summary = "Cambiar contraseña")
    @PutMapping("/me/contrasena")
    public ResponseEntity<Map<String, String>> cambiarContrasena(
            Principal principal,
            @RequestBody Map<String, String> body) {
        String actual = body.get("contrasenaActual");
        String nueva  = body.get("nuevaContrasena");
        if (actual == null || nueva == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Se requieren 'contrasenaActual' y 'nuevaContrasena'"));
        }
        usuarioService.cambiarContrasena(principal.getName(), actual, nueva);
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada correctamente."));
    }

    @Operation(summary = "Obtener mis logros/badges")
    @GetMapping("/me/logros")
    public ResponseEntity<List<Map<String, Object>>> misLogros(Principal principal) {
        return ResponseEntity.ok(usuarioService.obtenerLogros(principal.getName()));
    }

    @Operation(summary = "Cambiar preferencia de tema (CLARO | OSCURO)")
    @PutMapping("/me/tema")
    public ResponseEntity<UsuarioResponse> actualizarTema(
            Principal principal,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                usuarioService.actualizarTema(principal.getName(), body.get("tema"))
        );
    }

    /* ─── Ver perfil público de cualquier usuario ───────── */

    @Operation(summary = "Ver perfil de un usuario por ID")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> perfilPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    /* ─── PIN de seguridad ──────────────────────────────── */

    @Operation(summary = "Verificar si el usuario ya tiene PIN configurado")
    @GetMapping("/me/pin/estado")
    public ResponseEntity<Map<String, Object>> estadoPin(Principal principal) {
        boolean tiene = usuarioService.tienePinConfigurado(principal.getName());
        return ResponseEntity.ok(Map.of("tienePinConfigurado", tiene));
    }

    @Operation(summary = "Establecer o cambiar PIN de seguridad (4 dígitos)")
    @PutMapping("/me/pin")
    public ResponseEntity<Map<String, String>> establecerPin(
            Principal principal,
            @RequestBody Map<String, String> body) {
        String pinActual = body.get("pinActual");  // null si es primera vez
        String pinNuevo  = body.get("pinNuevo");
        if (pinNuevo == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Se requiere 'pinNuevo'"));
        }
        usuarioService.establecerPin(principal.getName(), pinActual, pinNuevo);
        return ResponseEntity.ok(Map.of("mensaje", "PIN de seguridad configurado correctamente."));
    }

    @Operation(summary = "Validar PIN antes de ejecutar una transferencia")
    @PostMapping("/me/pin/validar")
    public ResponseEntity<Map<String, Object>> validarPin(
            Principal principal,
            @RequestBody Map<String, String> body) {
        String pin = body.get("pin");
        boolean valido = usuarioService.validarPin(principal.getName(), pin);
        if (!valido) {
            // Retornar 200 con valido=false en lugar de 401
            // (401 causaría logout automático en el cliente)
            return ResponseEntity.ok(Map.of("valido", false, "mensaje", "PIN incorrecto."));
        }
        return ResponseEntity.ok(Map.of("valido", true));
    }

    /* ─── Admin ─────────────────────────────────────────── */

    @Operation(summary = "[Admin] Listar todos los usuarios")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @GetMapping("/admin/todos")
    public ResponseEntity<List<UsuarioResponse>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @Operation(summary = "[SuperAdmin] Listar solo administradores y superadmins")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @GetMapping("/admin/admins")
    public ResponseEntity<List<UsuarioResponse>> listarAdmins() {
        return ResponseEntity.ok(usuarioService.listarAdmins());
    }

    @Operation(summary = "[SuperAdmin] Crear un nuevo administrador")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @PostMapping("/admin/crear")
    public ResponseEntity<UsuarioResponse> crearAdmin(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(201).body(usuarioService.crearAdmin(req));
    }

    @Operation(summary = "[Admin] Activar o desactivar cuenta de usuario")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @PutMapping("/admin/{id}/toggle")
    public ResponseEntity<Map<String, String>> toggleActivo(@PathVariable Long id) {
        usuarioService.toggleActivo(id);
        return ResponseEntity.ok(Map.of("mensaje", "Estado de cuenta actualizado."));
    }

    @Operation(summary = "[Admin/SuperAdmin] Acreditarse DUNAB a sí mismo")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @PostMapping("/admin/acreditar-me")
    public ResponseEntity<UsuarioResponse> acreditarMe(
            Principal principal,
            @RequestBody Map<String, Integer> body) {
        Integer cantidad = body.getOrDefault("cantidad", 0);
        return ResponseEntity.ok(usuarioService.acreditarDunab(principal.getName(), cantidad));
    }

    @Operation(summary = "[SuperAdmin] Acreditar DUNAB a cualquier usuario")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @PostMapping("/admin/{id}/acreditar")
    public ResponseEntity<UsuarioResponse> acreditarUsuario(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        Integer cantidad = body.getOrDefault("cantidad", 0);
        return ResponseEntity.ok(usuarioService.acreditarDunabPorId(id, cantidad));
    }

    /* ─── Gestión de estudiantes (ADMIN / SUPERADMIN) ──── */

    @Operation(summary = "[Admin] Listar todos los estudiantes")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @GetMapping("/admin/estudiantes")
    public ResponseEntity<List<UsuarioResponse>> listarEstudiantes() {
        return ResponseEntity.ok(usuarioService.listarEstudiantes());
    }

    @Operation(summary = "[Admin] Editar datos de un estudiante por ID")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @PutMapping("/admin/{id}")
    public ResponseEntity<UsuarioResponse> editarEstudiante(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePerfilRequest req) {
        return ResponseEntity.ok(usuarioService.actualizarUsuarioPorId(id, req));
    }

    @Operation(summary = "[Admin] Eliminar un estudiante por ID")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Map<String, String>> eliminarEstudiante(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.ok(Map.of("mensaje", "Estudiante eliminado correctamente."));
    }

    @Operation(summary = "[Admin] Ajustar balance DUNAB de un estudiante (suma/resta)")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @PostMapping("/admin/{id}/ajustar-dunab")
    public ResponseEntity<UsuarioResponse> ajustarDunab(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Object montoObj = body.get("monto");
        if (montoObj == null) {
            return ResponseEntity.badRequest().build();
        }
        int monto = (montoObj instanceof Number)
                ? ((Number) montoObj).intValue()
                : Integer.parseInt(montoObj.toString());
        String motivo = body.get("motivo") != null ? body.get("motivo").toString() : null;
        return ResponseEntity.ok(usuarioService.ajustarDunab(id, monto, motivo));
    }
}
