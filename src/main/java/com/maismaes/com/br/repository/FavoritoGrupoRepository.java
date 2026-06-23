package com.maismaes.com.br.repository;

import com.maismaes.com.br.entities.grupo_tematico.FavoritoGrupo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoritoGrupoRepository extends JpaRepository<FavoritoGrupo, Long> {

  boolean existsByGrupoIdAndUsuarioId(Long grupoId, UUID usuarioId);

  Optional<FavoritoGrupo> findByGrupoIdAndUsuarioId(Long grupoId, UUID usuarioId);

  void deleteByGrupoIdAndUsuarioId(Long grupoId, UUID usuarioId);

  @EntityGraph(attributePaths = {"grupo", "grupo.criador", "grupo.bairros", "grupo.participantes"})
  List<FavoritoGrupo> findByUsuarioId(UUID usuarioId);
}
