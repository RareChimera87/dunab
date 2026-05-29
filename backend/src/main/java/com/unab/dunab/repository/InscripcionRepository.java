package com.unab.dunab.repository;

import com.unab.dunab.domain.entity.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    Optional<Inscripcion> findByUsuarioIdAndEncuentroId(Long usuarioId, Long encuentroId);
    boolean existsByUsuarioIdAndEncuentroId(Long usuarioId, Long encuentroId);
    List<Inscripcion> findByUsuarioIdOrderByInscritoEnDesc(Long usuarioId);
    List<Inscripcion> findByEncuentroId(Long encuentroId);

    @Query("SELECT COUNT(i) FROM Inscripcion i WHERE i.usuario.id = :uid AND i.asistio = true")
    long contarAsistencias(@Param("uid") Long usuarioId);
}
