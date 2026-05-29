package com.unab.dunab.repository;

import com.unab.dunab.domain.entity.Transferencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferenciaRepository extends JpaRepository<Transferencia, Long> {

    List<Transferencia> findByRemitenteIdOrderByCreadoEnDesc(Long remitenteId);
    List<Transferencia> findByDestinatarioIdOrderByCreadoEnDesc(Long destinatarioId);
}
