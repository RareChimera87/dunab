package com.unab.dunab.repository;

import com.unab.dunab.domain.entity.Logro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LogroRepository extends JpaRepository<Logro, Long> {
    Optional<Logro> findByCodigo(String codigo);
}
