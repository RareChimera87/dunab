package com.unab.dunab.service;

import com.unab.dunab.dto.request.LoginRequest;
import com.unab.dunab.dto.request.RegisterRequest;
import com.unab.dunab.dto.response.AuthResponse;
import com.unab.dunab.common.exception.DunabException;
import com.unab.dunab.domain.enums.Rol;
import com.unab.dunab.domain.entity.Usuario;
import com.unab.dunab.repository.UsuarioRepository;
import com.unab.dunab.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de autenticación: registro e inicio de sesión con JWT.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final LogroService logroService;

    /**
     * Registra un nuevo estudiante en el sistema.
     */
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (usuarioRepository.existsByCorreo(req.getCorreo())) {
            throw DunabException.conflict("Ya existe un usuario con el correo: " + req.getCorreo());
        }
        if (usuarioRepository.existsByCodigo(req.getCodigo())) {
            throw DunabException.conflict("Ya existe un usuario con el código: " + req.getCodigo());
        }

        Usuario usuario = Usuario.builder()
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
                .rol(Rol.ESTUDIANTE)
                .build();

        usuarioRepository.save(usuario);

        String token = jwtUtil.generateToken(usuario.getCorreo(), usuario.getRol().name());
        return buildAuthResponse(usuario, token);
    }

    /**
     * Autentica un usuario y retorna un JWT.
     */
    public AuthResponse login(LoginRequest req) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getCorreo(), req.getContrasena())
            );
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            Usuario usuario = usuarioRepository.findByCorreo(userDetails.getUsername())
                    .orElseThrow(() -> DunabException.notFound("Usuario no encontrado"));

            if (!usuario.getActivo()) {
                throw DunabException.forbidden("La cuenta está desactivada.");
            }

            String token = jwtUtil.generateToken(usuario.getCorreo(), usuario.getRol().name());
            return buildAuthResponse(usuario, token);

        } catch (BadCredentialsException e) {
            throw DunabException.badRequest("Correo o contraseña incorrectos.");
        }
    }

    private AuthResponse buildAuthResponse(Usuario u, String token) {
        return AuthResponse.builder()
                .token(token)
                .tipo("Bearer")
                .usuarioId(u.getId())
                .nombre(u.getNombre())
                .correo(u.getCorreo())
                .rol(u.getRol().name())
                .balanceDunab(u.getBalanceDunab())
                .temaPreferencia(u.getTemaPreferencia() != null ? u.getTemaPreferencia() : "CLARO")
                .facultad(u.getFacultad() != null ? u.getFacultad() : "Facultad de Ciencias Jurídicas y Políticas")
                .build();
    }
}
