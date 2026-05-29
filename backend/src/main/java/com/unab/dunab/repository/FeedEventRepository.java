package com.unab.dunab.repository;

import com.unab.dunab.domain.entity.FeedEvent;
import com.unab.dunab.domain.enums.FeedTipo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repositorio de eventos del feed de actividad.
 */
public interface FeedEventRepository extends JpaRepository<FeedEvent, Long> {

    /**
     * Feed global: todos los eventos no eliminados, más recientes primero.
     */
    @Query("SELECT f FROM FeedEvent f WHERE f.eliminado = false ORDER BY f.creadoEn DESC")
    Page<FeedEvent> findFeedGlobal(Pageable pageable);

    /**
     * Feed filtrado por tipo.
     */
    @Query("SELECT f FROM FeedEvent f WHERE f.eliminado = false AND f.tipo = :tipo ORDER BY f.creadoEn DESC")
    Page<FeedEvent> findFeedByTipo(@Param("tipo") FeedTipo tipo, Pageable pageable);

    /**
     * Eventos de un usuario específico (para su perfil).
     */
    @Query("SELECT f FROM FeedEvent f WHERE f.eliminado = false AND f.usuario.id = :uid ORDER BY f.creadoEn DESC")
    Page<FeedEvent> findByUsuarioId(@Param("uid") Long uid, Pageable pageable);

    /**
     * Verificar si ya existe un hito específico para un usuario
     * (evita publicar duplicados al recalcular).
     */
    @Query("SELECT f FROM FeedEvent f WHERE f.usuario.id = :uid AND f.tipo = 'HITO' " +
           "AND f.mensaje LIKE %:hito% AND f.eliminado = false ORDER BY f.creadoEn DESC")
    Optional<FeedEvent> findHitoExistente(@Param("uid") Long uid, @Param("hito") String hito);
}
