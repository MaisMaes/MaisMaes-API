package com.maismaes.com.br.repository;

import com.maismaes.com.br.entities.grupo_tematico.DenunciarGrupo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DenunciarGrupoRepository
    extends JpaRepository<DenunciarGrupo, Long>, JpaSpecificationExecutor<DenunciarGrupo> {

  // Busca todas as denúncias de um grupo
  List<DenunciarGrupo> findByGrupoId(Long grupo);

  // Busca todas as denúncias feitas por um usuário
  List<DenunciarGrupo> findByUsuarioId(UUID usuario);

  // Busca denúncia específica de um usuário em um grupo
  Optional<DenunciarGrupo> findByGrupoIdAndUsuarioId(Long grupo, UUID usuario);

  // Verifica se o usuário já denunciou o grupo
  boolean existsByGrupoIdAndUsuarioId(Long grupoId, UUID usuarioId);
}
