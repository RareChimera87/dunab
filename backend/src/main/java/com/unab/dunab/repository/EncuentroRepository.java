package com.unab.dunab.repository;

import com.unab.dunab.domain.entity.Encuentro;
import com.unab.dunab.domain.enums.EstadoEncuentro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EncuentroRepository extends JpaRepository<Encuentro, Long> {

    List<Encuentro> findByVisibleTrueOrderByFechaAsc();
    List<Encuentro> findByEstadoOrderByFechaAsc(EstadoEncuentro estado);

    @Query("SELECT e FROM Encuentro e WHERE e.visible = true " +
           "AND (:lugar IS NULL OR e.lugar = :lugar) " +
           "AND (:estado IS NULL OR e.estado = :estado) " +
           "AND (:desde IS NULL OR e.fecha >= :desde) " +
           "AND (:hasta IS NULL OR e.fecha <= :hasta) " +
           "ORDER BY e.fecha ASC")
    List<Encuentro> buscarFiltrado(@Param("lugar")  String lugar,
                                    @Param("estado") EstadoEncuentro estado,
                                    @Param("desde")  LocalDate desde,
                                    @Param("hasta")  LocalDate hasta);
}
