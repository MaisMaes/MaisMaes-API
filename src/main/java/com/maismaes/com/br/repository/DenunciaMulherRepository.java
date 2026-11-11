package com.maismaes.com.br.repository;

import com.maismaes.com.br.entities.denuncia.DenunciaMulher;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DenunciaMulherRepository extends JpaRepository<DenunciaMulher, Long> {
  Page<DenunciaMulher> findByUsuarioId(UUID usuarioId, Pageable pageable);
}
