package com.maismaes.com.br.repository;

import com.maismaes.com.br.entities.grupo_tematico.PedidoEntradaGrupo;
import com.maismaes.com.br.entities.grupo_tematico.StatusPedidoEntrada;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoEntradaGrupoRepository extends JpaRepository<PedidoEntradaGrupo, Long> {

  // Busca todos os pedidos de um grupo filtrados por status
  List<PedidoEntradaGrupo> findByGrupoIdAndStatus(Long grupoId, StatusPedidoEntrada status);

  // Busca pedido específico de um usuário em um grupo
  Optional<PedidoEntradaGrupo> findByGrupoIdAndUsuarioId(Long grupoId, UUID usuarioId);

  // Verifica se já existe pedido de um usuário em um grupo
  boolean existsByGrupoIdAndUsuarioId(Long grupoId, UUID usuarioId);
}

