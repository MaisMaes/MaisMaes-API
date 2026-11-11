package com.maismaes.com.br.controller;

import com.maismaes.com.br.dto.request.AtualizarStatusDenunciaMulherDTO;
import com.maismaes.com.br.dto.response.DenunciaMulherResponseDTO;
import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.entities.Role;
import com.maismaes.com.br.service.DenunciaMulherService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("denuncias")
public class DenunciaMulherController {

  private final DenunciaMulherService denunciaMulherService;

  @PostMapping("/criar")
  @Operation(summary = "Criar denúncia", description = "Cria uma nova denúncia para a mulher logada. Todos os dados são extraídos do perfil do usuário.")
  public ResponseEntity<DenunciaMulherResponseDTO> criarDenuncia(
      @AuthenticationPrincipal Perfil perfilLogado) {
    log.info("[DenunciaMulherController] criarDenuncia - Recebendo requisição de criação de denúncia");

    if (perfilLogado == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    DenunciaMulherResponseDTO resposta = denunciaMulherService.criarDenuncia(perfilLogado);

    return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
  }

  @GetMapping("/listar")
  @Operation(
      summary = "Listar denúncias",
      description = "Lista todas as denúncias da usuária logada com paginação")
  public ResponseEntity<Page<DenunciaMulherResponseDTO>> listarDenuncias(
      @AuthenticationPrincipal Perfil perfilLogado,
      @RequestParam(defaultValue = "0") int pagina,
      @RequestParam(defaultValue = "10") int tamanho) {
    log.info(
        "[DenunciaMulherController] listarDenuncias - Listando denúncias. Página: {}, Tamanho: {}",
        pagina,
        tamanho);

    if (perfilLogado == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    Page<DenunciaMulherResponseDTO> denuncias =
        denunciaMulherService.listarDenuncias(perfilLogado.getUsuario().getId(), pagina, tamanho);

    return ResponseEntity.ok(denuncias);
  }

  @PutMapping("/{id}/status")
  @Operation(summary = "Atualizar status", description = "Atualiza o status de uma denúncia")
  public ResponseEntity<DenunciaMulherResponseDTO> atualizarStatus(
      @PathVariable Long id,
      @RequestBody AtualizarStatusDenunciaMulherDTO dto,
      @AuthenticationPrincipal Perfil perfilLogado) {
    log.info(
        "[DenunciaMulherController] atualizarStatus - Atualizando status da denúncia ID: {}",
        id);

    if (perfilLogado == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    DenunciaMulherResponseDTO resposta = denunciaMulherService.atualizarStatus(id, dto.status());

    return ResponseEntity.ok(resposta);
  }

  @GetMapping("/admin/todas")
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  @Operation(
      summary = "Listar todas as denúncias (Admin)",
      description = "Lista TODAS as denúncias do sistema com paginação. Acesso exclusivo para administrador.")
  public ResponseEntity<Page<DenunciaMulherResponseDTO>> listarTodasDenuncias(
      @AuthenticationPrincipal Perfil perfilLogado,
      @RequestParam(defaultValue = "0") int pagina,
      @RequestParam(defaultValue = "10") int tamanho) {
    log.info(
        "[DenunciaMulherController] listarTodasDenuncias - Acesso admin. Página: {}, Tamanho: {}",
        pagina,
        tamanho);

    if (perfilLogado == null || perfilLogado.getRole() != Role.ADMINISTRADOR) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    Page<DenunciaMulherResponseDTO> denuncias =
        denunciaMulherService.listarTodasDenuncias(pagina, tamanho);

    return ResponseEntity.ok(denuncias);
  }
}
