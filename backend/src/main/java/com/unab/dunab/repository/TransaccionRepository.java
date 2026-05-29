package com.unab.dunab.repository;

import com.unab.dunab.domain.entity.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    List<Transaccion> findByUsuarioIdOrderByCreadoEnDesc(Long usuarioId);

    // ── Suma de ingresos en los últimos N meses ──────────────────────────────
    @Query(value = """
        SELECT COALESCE(SUM(monto), 0)
        FROM transacciones
        WHERE usuario_id = :uid
          AND tipo IN ('INGRESO','TRANSFERENCIA_RECIBIDA','RACHA_BONUS')
          AND creado_en >= NOW() - INTERVAL '1 month' * :meses
        """, nativeQuery = true)
    Integer sumarIngresosMeses(@Param("uid") Long usuarioId, @Param("meses") int meses);

    // ── Suma de egresos en los últimos N meses ───────────────────────────────
    @Query(value = """
        SELECT COALESCE(SUM(monto), 0)
        FROM transacciones
        WHERE usuario_id = :uid
          AND tipo IN ('EGRESO','TRANSFERENCIA_ENVIADA','PENALIZACION')
          AND creado_en >= NOW() - INTERVAL '1 month' * :meses
        """, nativeQuery = true)
    Integer sumarEgresosMeses(@Param("uid") Long usuarioId, @Param("meses") int meses);

    // ── Promedio de ingresos por semana (últimas 4 semanas) ──────────────────
    @Query(value = """
        SELECT COALESCE(AVG(semana.total), 0)
        FROM (
          SELECT DATE_TRUNC('week', creado_en) AS w, SUM(monto) AS total
          FROM transacciones
          WHERE usuario_id = :uid
            AND tipo IN ('INGRESO','TRANSFERENCIA_RECIBIDA','RACHA_BONUS')
            AND creado_en >= NOW() - INTERVAL '4 weeks'
          GROUP BY DATE_TRUNC('week', creado_en)
        ) semana
        """, nativeQuery = true)
    Double promedioSemanal(@Param("uid") Long usuarioId);

    // ── Promedio de ingresos por mes (últimos 12 meses) ──────────────────────
    @Query(value = """
        SELECT COALESCE(AVG(mes.total), 0)
        FROM (
          SELECT DATE_TRUNC('month', creado_en) AS m, SUM(monto) AS total
          FROM transacciones
          WHERE usuario_id = :uid
            AND tipo IN ('INGRESO','TRANSFERENCIA_RECIBIDA','RACHA_BONUS')
            AND creado_en >= NOW() - INTERVAL '12 months'
          GROUP BY DATE_TRUNC('month', creado_en)
        ) mes
        """, nativeQuery = true)
    Double promedioMensual(@Param("uid") Long usuarioId);

    // ── Promedio por semestre (últimos 6 meses) ──────────────────────────────
    @Query(value = """
        SELECT COALESCE(AVG(sem.total), 0)
        FROM (
          SELECT DATE_TRUNC('month', creado_en) AS m, SUM(monto) AS total
          FROM transacciones
          WHERE usuario_id = :uid
            AND tipo IN ('INGRESO','TRANSFERENCIA_RECIBIDA','RACHA_BONUS')
            AND creado_en >= NOW() - INTERVAL '6 months'
          GROUP BY DATE_TRUNC('month', creado_en)
        ) sem
        """, nativeQuery = true)
    Double promedioSemestral(@Param("uid") Long usuarioId);

    // ── Promedio por mes en el año actual ────────────────────────────────────
    @Query(value = """
        SELECT COALESCE(AVG(mes.total), 0)
        FROM (
          SELECT DATE_TRUNC('month', creado_en) AS m, SUM(monto) AS total
          FROM transacciones
          WHERE usuario_id = :uid
            AND tipo IN ('INGRESO','TRANSFERENCIA_RECIBIDA','RACHA_BONUS')
            AND EXTRACT(YEAR FROM creado_en) = EXTRACT(YEAR FROM NOW())
          GROUP BY DATE_TRUNC('month', creado_en)
        ) mes
        """, nativeQuery = true)
    Double promedioAnual(@Param("uid") Long usuarioId);
}
