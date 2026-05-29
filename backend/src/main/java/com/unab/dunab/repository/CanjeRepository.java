package com.unab.dunab.repository;

import com.unab.dunab.domain.entity.Canje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CanjeRepository extends JpaRepository<Canje, Long> {

    /** Canjes de un estudiante, más recientes primero. */
    @Query("SELECT c FROM Canje c WHERE c.usuario.id = :usuarioId ORDER BY c.canjeadoEn DESC")
    List<Canje> findByUsuario(@Param("usuarioId") Long usuarioId);

    /** Todos los canjes para la vista admin (más recientes primero). */
    @Query("SELECT c FROM Canje c ORDER BY c.canjeadoEn DESC")
    List<Canje> findAllOrdenados();

    /** Buscar canje por código (para validación). */
    Optional<Canje> findByCodigoCanje(String codigoCanje);

    /** Cantidad de canjes pendientes (para badge en el admin). */
    long countByEstado(String estado);
}
