package com.unab.dunab.repository;

import com.unab.dunab.domain.entity.UsuarioLogro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioLogroRepository extends JpaRepository<UsuarioLogro, Long> {

    List<UsuarioLogro> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndLogroId(Long usuarioId, Long logroId);

    long countByUsuarioId(Long usuarioId);
}
