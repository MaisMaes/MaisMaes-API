package com.maismaes.com.br.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.maismaes.com.br.entities.grupo_tematico.ParticipanteGrupo;

public interface ParticipanteGrupoRepository extends JpaRepository<ParticipanteGrupo, Long> {

    // Busca o vínculo de uma usuária específica em um grupo específico
    Optional<ParticipanteGrupo> findByGrupoIdAndUsuarioId(Long grupoId, UUID usuarioId);

    // Busca todos os vínculos de um usuário (para listar os grupos em que participa)
    List<ParticipanteGrupo> findByUsuarioId(UUID usuarioId);

    // Verifica se o usuário já pertence ao grupo
    boolean existsByGrupoIdAndUsuarioId(Long grupoId, UUID usuarioId);
}
