package com.maismaes.com.br.controller;

import com.maismaes.com.br.dto.request.CriarGrupoTematicoRequestDTO;
import com.maismaes.com.br.dto.request.EditarGrupoTematicoRequestDTO;
import com.maismaes.com.br.dto.response.DetalheGrupoResponseDTO;
import com.maismaes.com.br.dto.response.EditarGrupoTematicoResponseDTO;
import com.maismaes.com.br.dto.response.GrupoTematicoResponseDTO;
import com.maismaes.com.br.dto.response.ListarGrupoTematicoDTO;
import com.maismaes.com.br.dto.response.MembroStatusResponseDTO;
import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.entities.grupo_tematico.GrupoTematico;
import com.maismaes.com.br.service.GrupoTematicoService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.maismaes.com.br.dto.request.CriarGrupoTematicoRequestDTO;
import com.maismaes.com.br.dto.request.EditarGrupoTematicoRequestDTO;
import com.maismaes.com.br.dto.response.DetalheGrupoResponseDTO;
import com.maismaes.com.br.dto.response.EditarGrupoTematicoResponseDTO;
import com.maismaes.com.br.dto.response.GrupoTematicoResponseDTO;
import com.maismaes.com.br.dto.response.ListarGrupoTematicoDTO;
import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.entities.grupo_tematico.GrupoTematico;
import com.maismaes.com.br.service.GrupoTematicoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    var grupoCriado =
        grupoTematicoService.criarGrupoTematico(
            grupoTematicoRequestDTO.ToGrupoTematicoEntity(),
            grupoTematicoRequestDTO.bairros(),
            perfilLogado);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new GrupoTematicoResponseDTO(grupoCriado.getId()));
  }

  // @PatchMapping("/{grupoId}/membros/{usuarioId}/privilegio")
  // public ResponseEntity<String> mudarPrivilegio(
  //         @PathVariable Long grupoId,
  //         @PathVariable UUID usuarioId,
  //         @RequestParam GrupoRole novaRole,
  //         @AuthenticationPrincipal Perfil perfilLogado) {
  //     grupoTematicoService.alterarPrivilegio(grupoId, usuarioId, novaRole, perfilLogado);
  //     return ResponseEntity.ok("Privilégio atualizado com sucesso!");
  // }

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

  // Entrar em um grupo
  @PostMapping("/{id}/entrar")
  public ResponseEntity<String> entrarNoGrupo(
      @PathVariable Long id, @AuthenticationPrincipal Perfil perfilLogado) {
    log.info("[REQUISIÇÃO] - Chegada de chamda para entrar em grupo");
    grupoTematicoService.entrarNoGrupo(id, perfilLogado);
    return ResponseEntity.ok("Você entrou no grupo com sucesso!");
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
}
