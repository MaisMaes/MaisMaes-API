package com.maismaes.com.br.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.maismaes.com.br.entities.grupo_tematico.ParticipanteGrupo;

public interface ParticipanteGrupoRepository extends JpaRepository<ParticipanteGrupo, Long> {

    // Busca o vínculo de uma usuária específica em um grupo específico
    Optional<ParticipanteGrupo> findByGrupoIdAndUsuarioId(Long grupoId, UUID usuarioId);
}
