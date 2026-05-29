package com.unab.dunab.service;

import com.unab.dunab.dto.request.RegisterRequest;
import com.unab.dunab.dto.request.UpdatePerfilRequest;
import com.unab.dunab.dto.response.UsuarioResponse;
import com.unab.dunab.common.exception.DunabException;
import com.unab.dunab.domain.enums.Rol;
import com.unab.dunab.domain.enums.TipoTransaccion;
import com.unab.dunab.domain.entity.Usuario;
import com.unab.dunab.domain.entity.UsuarioLogro;
import com.unab.dunab.repository.InscripcionRepository;
import com.unab.dunab.repository.TransaccionRepository;
import com.unab.dunab.repository.TransferenciaRepository;
import com.unab.dunab.repository.UsuarioLogroRepository;
import com.unab.dunab.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para gestión del perfil y datos de usuario.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final LogroService logroService;
    private final TransaccionService transaccionService;
    // Repositorios necesarios para eliminación en cascada manual
    private final TransferenciaRepository transferenciaRepository;
    private final InscripcionRepository inscripcionRepository;
    private final TransaccionRepository transaccionRepository;
    private final UsuarioLogroRepository usuarioLogroRepository;

    /* ─── Consultas ─────────────────────────────────────── */

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorCorreo(String correo) {
        Usuario u = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));
        return toResponse(u);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));
        return toResponse(u);
    }

    /* ─── Actualización de perfil ───────────────────────── */

    @Transactional
    public UsuarioResponse actualizarPerfil(String correo, UpdatePerfilRequest req) {
        Usuario u = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));

        if (req.getNombre()   != null) u.setNombre(req.getNombre());
        if (req.getCelular()  != null) u.setCelular(req.getCelular());
        if (req.getCarrera()  != null) u.setCarrera(req.getCarrera());
        if (req.getFacultad() != null && !req.getFacultad().isBlank()) u.setFacultad(req.getFacultad());
        if (req.getSemestre() != null) u.setSemestre(req.getSemestre());
        if (req.getCedula()   != null) u.setCedula(req.getCedula());

        return toResponse(usuarioRepository.save(u));
    }

    /* ─── Cambio de contraseña ──────────────────────────── */

    @Transactional
    public void cambiarContrasena(String correo, String contrasenaActual, String nuevaContrasena) {
        Usuario u = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));

        if (!passwordEncoder.matches(contrasenaActual, u.getContrasena())) {
            throw DunabException.badRequest("La contraseña actual es incorrecta.");
        }
        u.setContrasena(passwordEncoder.encode(nuevaContrasena));
        usuarioRepository.save(u);
    }

    /* ─── Logros del usuario ────────────────────────────── */

    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerLogros(String correo) {
        Usuario u = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));

        return logroService.obtenerLogrosDeUsuario(u.getId())
                .stream()
                .map(ul -> Map.<String, Object>of(
                        "codigo",      ul.getLogro().getCodigo(),
                        "nombre",      ul.getLogro().getNombre(),
                        "descripcion", ul.getLogro().getDescripcion() != null ? ul.getLogro().getDescripcion() : "",
                        "emoji",       ul.getLogro().getEmoji() != null ? ul.getLogro().getEmoji() : "",
                        "obtenidoEn",  ul.getObtenidoEn().toString()
                ))
                .collect(Collectors.toList());
    }

    /* ─── PIN de seguridad ──────────────────────────────── */

    /**
     * Establece o actualiza el PIN de 4 dígitos del usuario.
     * Si ya tiene PIN, requiere que confirme el PIN actual.
     */
    @Transactional
    public void establecerPin(String correo, String pinActual, String pinNuevo) {
        if (pinNuevo == null || !pinNuevo.matches("\\d{4}")) {
            throw DunabException.badRequest("El PIN debe ser exactamente 4 dígitos numéricos.");
        }
        Usuario u = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));

        if (u.getPinSeguridad() != null && !u.getPinSeguridad().isBlank()) {
            // Ya tiene PIN configurado: validar el actual antes de cambiar
            if (pinActual == null || !passwordEncoder.matches(pinActual, u.getPinSeguridad())) {
                throw DunabException.badRequest("El PIN actual es incorrecto.");
            }
        }
        u.setPinSeguridad(passwordEncoder.encode(pinNuevo));
        usuarioRepository.save(u);
    }

    /** Valida el PIN ingresado contra el almacenado. */
    @Transactional(readOnly = true)
    public boolean validarPin(String correo, String pin) {
        if (pin == null) return false;
        Usuario u = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));
        if (u.getPinSeguridad() == null || u.getPinSeguridad().isBlank()) return false;
        return passwordEncoder.matches(pin, u.getPinSeguridad());
    }

    /** Indica si el usuario ya tiene PIN configurado (sin exponerlo). */
    @Transactional(readOnly = true)
    public boolean tienePinConfigurado(String correo) {
        Usuario u = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));
        return u.getPinSeguridad() != null && !u.getPinSeguridad().isBlank();
    }

    /* ─── Admin: crear otro administrador ──────────────── */

    @Transactional
    public UsuarioResponse crearAdmin(RegisterRequest req) {
        if (usuarioRepository.existsByCorreo(req.getCorreo())) {
            throw DunabException.conflict("Ya existe un usuario con el correo: " + req.getCorreo());
        }
        if (usuarioRepository.existsByCodigo(req.getCodigo())) {
            throw DunabException.conflict("Ya existe un usuario con el código: " + req.getCodigo());
        }
        Usuario u = Usuario.builder()
                .nombre(req.getNombre())
                .correo(req.getCorreo())
                .contrasena(passwordEncoder.encode(req.getContrasena()))
                .codigo(req.getCodigo())
                .carrera(req.getCarrera())
                .facultad(req.getFacultad() != null && !req.getFacultad().isBlank()
                        ? req.getFacultad()
                        : "Facultad de Ciencias Jurídicas y Políticas")
                .semestre(req.getSemestre() != null ? req.getSemestre() : 1)
                .celular(req.getCelular())
                .cedula(req.getCedula())
                .rol(Rol.ADMIN)
                .build();
        usuarioRepository.save(u);
        return toResponse(u);
    }

    /* ─── Admin: listar todos ───────────────────────────── */

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<UsuarioResponse> listarAdmins() {
        return usuarioRepository.findAll()
                .stream()
                .filter(u -> u.getRol() == Rol.ADMIN || u.getRol() == Rol.SUPERADMIN)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void toggleActivo(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));
        u.setActivo(!u.getActivo());
        usuarioRepository.save(u);
    }

    /**
     * Acredita DUNAB al admin/superadmin autenticado.
     * Máximo 10 000 DUNAB por operación para evitar abusos accidentales.
     */
    @Transactional
    public UsuarioResponse acreditarDunab(String correo, int cantidad) {
        if (cantidad <= 0 || cantidad > 10_000) {
            throw DunabException.badRequest("La cantidad debe estar entre 1 y 10 000 DUNAB.");
        }
        Usuario u = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));
        u.acreditarDunab(cantidad);
        usuarioRepository.save(u);
        return toResponse(u);
    }

    /**
     * SUPERADMIN acredita DUNAB a cualquier usuario por ID.
     */
    @Transactional
    public UsuarioResponse acreditarDunabPorId(Long id, int cantidad) {
        if (cantidad <= 0 || cantidad > 10_000) {
            throw DunabException.badRequest("La cantidad debe estar entre 1 y 10 000 DUNAB.");
        }
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));
        u.acreditarDunab(cantidad);
        usuarioRepository.save(u);
        return toResponse(u);
    }

    /* ─── Gestión de estudiantes (ADMIN / SUPERADMIN) ───── */

    /** Lista únicamente usuarios con rol ESTUDIANTE. */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarEstudiantes() {
        return usuarioRepository.findAll()
                .stream()
                .filter(u -> u.getRol() == Rol.ESTUDIANTE)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Edita los datos de un usuario por ID. Pensado para que un admin
     * pueda actualizar la información de un estudiante.
     * No permite editar admins/superadmins desde esta vía para evitar escalada.
     */
    @Transactional
    public UsuarioResponse actualizarUsuarioPorId(Long id, UpdatePerfilRequest req) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));

        if (u.getRol() != Rol.ESTUDIANTE) {
            throw DunabException.badRequest("Solo se pueden editar estudiantes desde esta operación.");
        }

        if (req.getNombre()   != null) u.setNombre(req.getNombre());
        if (req.getCelular()  != null) u.setCelular(req.getCelular());
        if (req.getCarrera()  != null) u.setCarrera(req.getCarrera());
        if (req.getFacultad() != null && !req.getFacultad().isBlank()) u.setFacultad(req.getFacultad());
        if (req.getSemestre() != null) u.setSemestre(req.getSemestre());
        if (req.getCedula()   != null) u.setCedula(req.getCedula());
        // Correo se permite, pero validando unicidad
        if (req.getCorreo() != null && !req.getCorreo().equalsIgnoreCase(u.getCorreo())) {
            if (usuarioRepository.existsByCorreo(req.getCorreo())) {
                throw DunabException.conflict("Ya existe un usuario con el correo: " + req.getCorreo());
            }
            u.setCorreo(req.getCorreo());
        }

        return toResponse(usuarioRepository.save(u));
    }

    /**
     * Elimina por completo a un estudiante. Solo aplica a rol ESTUDIANTE
     * para evitar que un admin elimine a otros admins o al superadmin.
     *
     * Borra en cascada manual (en orden correcto para respetar FK):
     *   1. Transferencias (como remitente y como destinatario)
     *   2. Inscripciones
     *   3. Transacciones
     *   4. Logros del usuario
     *   5. Usuario
     */
    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));
        if (u.getRol() != Rol.ESTUDIANTE) {
            throw DunabException.badRequest("Solo se pueden eliminar estudiantes desde esta operación.");
        }

        // 1. Transferencias donde fue remitente o destinatario
        transferenciaRepository.deleteAll(
                transferenciaRepository.findByRemitenteIdOrderByCreadoEnDesc(id));
        transferenciaRepository.deleteAll(
                transferenciaRepository.findByDestinatarioIdOrderByCreadoEnDesc(id));

        // 2. Inscripciones a encuentros
        inscripcionRepository.deleteAll(
                inscripcionRepository.findByUsuarioIdOrderByInscritoEnDesc(id));

        // 3. Historial de transacciones
        transaccionRepository.deleteAll(
                transaccionRepository.findByUsuarioIdOrderByCreadoEnDesc(id));

        // 4. Logros obtenidos
        usuarioLogroRepository.deleteAll(
                usuarioLogroRepository.findByUsuarioId(id));

        // 5. Finalmente, eliminar el usuario
        usuarioRepository.delete(u);
    }

    /**
     * Ajusta el balance DUNAB de un usuario (positivo = sumar, negativo = restar).
     * Registra automáticamente una transacción de tipo INGRESO o EGRESO.
     */
    @Transactional
    public UsuarioResponse ajustarDunab(Long id, int monto, String motivo) {
        if (monto == 0) {
            throw DunabException.badRequest("El monto debe ser distinto de cero.");
        }
        if (Math.abs(monto) > 10_000) {
            throw DunabException.badRequest("El ajuste máximo por operación es de 10 000 DUNAB.");
        }

        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));

        String descripcion = "Ajuste administrativo: " +
                (motivo == null || motivo.isBlank() ? "sin motivo especificado" : motivo.trim());

        if (monto > 0) {
            u.acreditarDunab(monto);
            usuarioRepository.save(u);
            transaccionService.registrar(u, TipoTransaccion.INGRESO, monto, descripcion, null);
        } else {
            int absMonto = Math.abs(monto);
            if (!u.tieneSaldo(absMonto)) {
                throw DunabException.badRequest("Saldo insuficiente para aplicar el descuento.");
            }
            u.debitarDunab(absMonto);
            usuarioRepository.save(u);
            transaccionService.registrar(u, TipoTransaccion.EGRESO, absMonto, descripcion, null);
        }

        return toResponse(u);
    }

    /* ─── Mapper ─────────────────────────────────────────── */

    public UsuarioResponse toResponse(Usuario u) {
        return UsuarioResponse.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .correo(u.getCorreo())
                .codigo(u.getCodigo())
                .cedula(u.getCedula())
                .celular(u.getCelular())
                .carrera(u.getCarrera())
                .facultad(u.getFacultad() != null ? u.getFacultad() : "Facultad de Ciencias Jurídicas y Políticas")
                .semestre(u.getSemestre())
                .rol(u.getRol().name())
                .balanceDunab(u.getBalanceDunab())
                .rachaDias(u.getRachaDias())
                .activo(u.getActivo())
                .temaPreferencia(u.getTemaPreferencia() != null ? u.getTemaPreferencia() : "CLARO")
                .creadoEn(u.getCreadoEn())
                .build();
    }

    /* ─── Preferencia de tema (claro/oscuro) ────────────── */

    /**
     * Actualiza la preferencia de tema del usuario autenticado.
     * Valores aceptados: "CLARO" | "OSCURO" (case-insensitive).
     */
    @Transactional
    public UsuarioResponse actualizarTema(String correo, String tema) {
        if (tema == null) {
            throw DunabException.badRequest("Se requiere 'tema'.");
        }
        String normalizado = tema.trim().toUpperCase();
        if (!normalizado.equals("CLARO") && !normalizado.equals("OSCURO")) {
            throw DunabException.badRequest("Tema inválido. Valores aceptados: CLARO | OSCURO.");
        }
        Usuario u = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));
        u.setTemaPreferencia(normalizado);
        return toResponse(usuarioRepository.save(u));
    }
}
