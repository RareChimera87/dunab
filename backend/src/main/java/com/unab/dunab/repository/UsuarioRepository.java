package com.unab.dunab.repository;

import com.unab.dunab.domain.entity.Usuario;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByCodigo(String codigo);
    boolean existsByCorreo(String correo);
    boolean existsByCodigo(String codigo);

    /** Ranking completo ordenado por balance descendente (solo estudiantes activos). */
    @Query("SELECT u FROM Usuario u WHERE u.activo = true AND u.rol = com.unab.dunab.domain.enums.Rol.ESTUDIANTE ORDER BY u.balanceDunab DESC")
    List<Usuario> findRanking();

    /** Top-N del ranking usando Pageable para limitar resultados (solo estudiantes). */
    @Query("SELECT u FROM Usuario u WHERE u.activo = true AND u.rol = com.unab.dunab.domain.enums.Rol.ESTUDIANTE ORDER BY u.balanceDunab DESC")
    List<Usuario> findTopN(Pageable pageable);
}
