package com.maismaes.com.br.controller;

import com.maismaes.com.br.dto.request.*;
import com.maismaes.com.br.dto.response.DetalheGrupoResponseDTO;
import com.maismaes.com.br.dto.response.EditarGrupoTematicoResponseDTO;
import com.maismaes.com.br.dto.response.GrupoTematicoResponseDTO;
import com.maismaes.com.br.dto.response.ListarGrupoTematicoDTO;
import com.maismaes.com.br.dto.response.MembroStatusResponseDTO;
import com.maismaes.com.br.dto.response.PedidoEntradaResponseDTO;
import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.entities.grupo_tematico.DenunciarGrupo;
import com.maismaes.com.br.entities.grupo_tematico.GrupoTematico;
import com.maismaes.com.br.service.GrupoTematicoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("grupo-tematico")
public class GrupoTematicoController {

  private final GrupoTematicoService grupoTematicoService;

  @PostMapping("/criar")
  public ResponseEntity<GrupoTematicoResponseDTO> criarGrupoTematico(
      @RequestBody @Valid CriarGrupoTematicoRequestDTO grupoTematicoRequestDTO,
      @AuthenticationPrincipal Perfil perfilLogado) {
    // Passamos a entidade, a LISTA de bairros (Strings) e o perfil logado
    GrupoTematico grupoCriado =
        grupoTematicoService.criarGrupoTematico(
            grupoTematicoRequestDTO.ToGrupoTematicoEntity(),
            grupoTematicoRequestDTO.bairros(),
            perfilLogado);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new GrupoTematicoResponseDTO(grupoCriado.getId()));
  }

  // Editar grupo
  @PutMapping("/editar/{id}")
  public ResponseEntity<EditarGrupoTematicoResponseDTO> atualizarGrupo(
      @PathVariable Long id,
      @RequestBody @Valid EditarGrupoTematicoRequestDTO updateDTO,
      @AuthenticationPrincipal Perfil perfilLogado) {

    GrupoTematico grupo = grupoTematicoService.atualizarGrupo(id, updateDTO, perfilLogado);

    EditarGrupoTematicoResponseDTO response = new EditarGrupoTematicoResponseDTO(grupo);

    return ResponseEntity.ok(response);
  }

  // Listar todos os grupos
  @GetMapping("/listar")
  public ResponseEntity<List<ListarGrupoTematicoDTO>> listarGrupos() {
    List<ListarGrupoTematicoDTO> grupos = grupoTematicoService.listarTodos();
    return ResponseEntity.ok(grupos);
  }

  // Buscar grupo peli id - detalhe do grupo
  @GetMapping("/detalhes/{id}")
  public ResponseEntity<DetalheGrupoResponseDTO> buscarDetalhes(
      @PathVariable Long id, @AuthenticationPrincipal Perfil perfilLogado) {

    UUID usuarioLogadoId = perfilLogado.getUsuario().getId();

    DetalheGrupoResponseDTO response = grupoTematicoService.obterDetalhes(id, usuarioLogadoId);

    return ResponseEntity.ok(response);
  }

  // Pesquisar grupos(GLOBAL)
  @GetMapping("/pesquisar")
  public ResponseEntity<List<ListarGrupoTematicoDTO>> pesquisar(
      @RequestParam(required = false) String termo) {
    return ResponseEntity.ok(grupoTematicoService.pesquisarGrupoTematico(termo));
  }

  // Entrar em um grupo (público) ou solicitar entrada (privado)
  @Operation(
      summary = "Entrar ou solicitar entrada em um grupo",
      description =
          "Se o grupo for público, o usuário entra diretamente. Se for privado, um pedido de entrada é criado e aguarda aprovação da criadora.")
  @PostMapping("/{id}/entrar")
  public ResponseEntity<String> entrarNoGrupo(
      @PathVariable Long id, @AuthenticationPrincipal Perfil perfilLogado) {
    log.info("[REQUISIÇÃO] - Chegada de chamada para entrar em grupo");
    String mensagem = grupoTematicoService.entrarNoGrupo(id, perfilLogado);
    return ResponseEntity.ok(mensagem);
  }

  // Listar pedidos de entrada pendentes de um grupo
  @Operation(
      summary = "Listar pedidos de entrada pendentes",
      description = "Retorna os pedidos de entrada com status PENDENTE. Apenas criadora ou moderadora podem acessar.")
  @GetMapping("/{id}/pedidos-entrada")
  public ResponseEntity<List<PedidoEntradaResponseDTO>> listarPedidosPendentes(
      @PathVariable Long id, @AuthenticationPrincipal Perfil perfilLogado) {
    log.info("[REQUISIÇÃO] - Listando pedidos de entrada pendentes do grupo {}", id);
    return ResponseEntity.ok(grupoTematicoService.listarPedidosPendentes(id, perfilLogado));
  }

  // Aprovar ou rejeitar pedido de entrada
  @Operation(
      summary = "Responder pedido de entrada",
      description = "Aprova ou rejeita um pedido de entrada em grupo privado. Apenas criadora ou moderadora podem responder.")
  @PatchMapping("/{grupoId}/pedidos-entrada/{pedidoId}")
  public ResponseEntity<PedidoEntradaResponseDTO> responderPedido(
      @PathVariable Long grupoId,
      @PathVariable Long pedidoId,
      @RequestBody @Valid ResponderPedidoRequestDTO dto,
      @AuthenticationPrincipal Perfil perfilLogado) {
    log.info(
        "[REQUISIÇÃO] - Respondendo pedido {} do grupo {}. Aprovado: {}",
        pedidoId,
        grupoId,
        dto.aprovado());
    return ResponseEntity.ok(
        grupoTematicoService.responderPedido(grupoId, pedidoId, dto.aprovado(), perfilLogado));
  }

  // Listar grupos que o usuário participa
  @GetMapping("/meus-grupos")
  public ResponseEntity<List<ListarGrupoTematicoDTO>> meusGrupos(
      @AuthenticationPrincipal Perfil perfilLogado) {
    return ResponseEntity.ok(grupoTematicoService.listarGruposDoUsuario(perfilLogado));
  }

  @GetMapping("/{id}/sou-participante")
  public ResponseEntity<MembroStatusResponseDTO> souParticipante(
      @PathVariable Long id, @AuthenticationPrincipal Perfil perfilLogado) {
    return ResponseEntity.ok(grupoTematicoService.verificarParticipacao(id, perfilLogado));
  }

  // Favoritar grupo
  @PostMapping("/{id}/favoritos")
  public ResponseEntity<Void> favoritarGrupo(
      @PathVariable Long id, @AuthenticationPrincipal Perfil perfilLogado) {
    grupoTematicoService.favoritarGrupo(id, perfilLogado);
    return ResponseEntity.ok().build();
  }

  // Remover grupo dos favoritos
  @DeleteMapping("/{id}/favoritos")
  public ResponseEntity<Void> removerFavoritoGrupo(
      @PathVariable Long id, @AuthenticationPrincipal Perfil perfilLogado) {
    grupoTematicoService.removerFavoritoGrupo(id, perfilLogado);
    return ResponseEntity.noContent().build();
  }

  // Listar grupos favoritados pelo usuario
  @GetMapping("/favoritos")
  public ResponseEntity<List<ListarGrupoTematicoDTO>> listarFavoritos(
      @AuthenticationPrincipal Perfil perfilLogado) {
    return ResponseEntity.ok(grupoTematicoService.listarGruposFavoritos(perfilLogado));
  }

  // Deletar grupo
  @DeleteMapping("/excluir/{id}")
  public ResponseEntity<String> excluirGrupo(
      @PathVariable Long id, @AuthenticationPrincipal Perfil perfilLogado) {
    try {
      grupoTematicoService.excluirGrupo(id, perfilLogado);

      return ResponseEntity.ok("Grupo excluído com sucesso");

    } catch (RuntimeException e) {

      if (e.getMessage().contains("permissão")) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
      }
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
  }

  // Criar denúncia (DEnunciar grupo tematico)
  @PostMapping("/denunciar/{id}")
  public ResponseEntity<String> denunciarGrupo(
      @PathVariable Long id,
      @AuthenticationPrincipal Perfil perfilLogado,
      @RequestBody @Valid CriarDenunciaGrupoRequestDTO request) {

    grupoTematicoService.denunciarGrupo(id, perfilLogado, request.descricao());

    System.out.println("Denúncia registrada com sucesso!");

    return ResponseEntity.status(HttpStatus.CREATED).body("Denúncia registrada com sucesso!");
  }

  @Operation(summary = "Banir participante de um grupo")
  @PatchMapping("/{grupoId}/banir")
  public ResponseEntity<Void> banirParticipante(
      @PathVariable Long grupoId,
      @RequestBody @Valid BanirParticipanteRequestDTO dto,
      @AuthenticationPrincipal Perfil perfilLogado) {

    grupoTematicoService.banirParticipante(grupoId, dto.usuarioId(), dto.motivo(), perfilLogado);

    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Remover participante de um grupo")
  @DeleteMapping("/{grupoId}/participantes/{usuarioId}")
  public ResponseEntity<String> removerParticipante(
      @PathVariable Long grupoId,
      @PathVariable UUID usuarioId,
      @AuthenticationPrincipal Perfil perfilLogado) {

    grupoTematicoService.removerParticipanteDoGrupo(grupoId, usuarioId, perfilLogado);
    return ResponseEntity.ok("Participante removido do grupo com sucesso.");
  }

  @Operation(summary = "Buscar Denuncias, aceita filtros")
  @GetMapping
  public ResponseEntity<Page<DenunciaGrupoResponseDTO>> buscarDenuncias(
      DenunciaGrupoFilterDTO filtro,
      @RequestParam(defaultValue = "0") int pagina,
      @RequestParam(defaultValue = "10") int tamanho) {

    Page<DenunciaGrupoResponseDTO> paginaResultado =
        grupoTematicoService.listarDenuncias(filtro, pagina, tamanho);
    return ResponseEntity.ok(paginaResultado);
  }

  @Operation(summary = "Atualiza campos específicos de uma denúncia (PATCH)")
  @PatchMapping("/{id}")
  public ResponseEntity<DenunciaGrupoResponseDTO> atualizarDenuncia(
      @PathVariable Long id, @RequestBody AtualizarDenunciaDTO dto) {

    DenunciarGrupo denunciaAtualizada = grupoTematicoService.atualizarParcial(id, dto);

    // Retorna o DTO de resposta limpo que criamos na etapa anterior
    return ResponseEntity.ok(new DenunciaGrupoResponseDTO(denunciaAtualizada));
  }

  @Operation(summary = "Sair de um grupo temático")
  @DeleteMapping("/{id}/sair")
  public ResponseEntity<String> sairDoGrupo(
      @PathVariable Long id, @AuthenticationPrincipal Perfil perfilLogado) {
    log.info(
        "[REQUISIÇÃO] - Usuário {} tentando sair do grupo {}",
        perfilLogado.getUsuario().getId(),
        id);
    try {
      grupoTematicoService.sairDoGrupo(id, perfilLogado);
      return ResponseEntity.ok("Você saiu do grupo com sucesso!");
    } catch (RuntimeException e) {
      log.warn(
          "[REQUISIÇÃO] - Erro ao sair do grupo {}: {}",
          id,
          e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }

}

