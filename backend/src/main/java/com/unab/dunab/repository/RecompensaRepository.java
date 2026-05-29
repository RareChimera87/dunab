package com.unab.dunab.repository;

import com.unab.dunab.domain.entity.Recompensa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecompensaRepository extends JpaRepository<Recompensa, Long> {

    /** Todas las recompensas activas ordenadas por categoría y nombre. */
    @Query("SELECT r FROM Recompensa r WHERE r.activa = true ORDER BY r.categoria, r.nombre")
    List<Recompensa> findActivas();

    /** Todas las recompensas (activas e inactivas) para la vista admin. */
    @Query("SELECT r FROM Recompensa r ORDER BY r.creadoEn DESC")
    List<Recompensa> findAllOrdenadas();
}
