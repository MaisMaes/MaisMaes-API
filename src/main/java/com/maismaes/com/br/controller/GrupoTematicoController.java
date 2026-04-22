package com.maismaes.com.br.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.maismaes.com.br.dto.request.BuscaGrupoTeamaticoDTO;
import com.maismaes.com.br.dto.request.EditarGrupoTematicoRequestDTO;
import com.maismaes.com.br.dto.request.GrupoTematicoRequestDTO;
import com.maismaes.com.br.dto.response.GrupoTematicoResponseDTO;
import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.entities.grupo_tematico.GrupoRole;
import com.maismaes.com.br.entities.grupo_tematico.GrupoTematico;
import com.maismaes.com.br.service.GrupoTematicoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("grupo-tematico")
public class GrupoTematicoController {

    private final GrupoTematicoService grupoTematicoService;

    @PostMapping("/criar")
    public ResponseEntity<GrupoTematicoResponseDTO> criarGrupoTematico(
            @RequestBody @Valid GrupoTematicoRequestDTO grupoTematicoRequestDTO,
            @AuthenticationPrincipal Perfil perfilLogado // Verifica pelo spring se o usuário está autenticado e passa o

    ) {

        var grupoCriado = grupoTematicoService.criarGrupoTematico(
                grupoTematicoRequestDTO.ToGrupoTematicoEntity(),
                perfilLogado);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new GrupoTematicoResponseDTO(grupoCriado.getId()));
    }

    @PatchMapping("/{grupoId}/membros/{usuarioId}/privilegio")
    public ResponseEntity<String> mudarPrivilegio(
            @PathVariable Long grupoId,
            @PathVariable UUID usuarioId,
            @RequestParam GrupoRole novaRole,
            @AuthenticationPrincipal Perfil perfilLogado) {
        grupoTematicoService.alterarPrivilegio(grupoId, usuarioId, novaRole, perfilLogado);
        return ResponseEntity.ok("Privilégio atualizado com sucesso!");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> atualizarGrupo(
            @PathVariable Long id,
            @RequestBody @Valid EditarGrupoTematicoRequestDTO updateDTO,
            @AuthenticationPrincipal Perfil perfilLogado) {
        grupoTematicoService.atualizarGrupo(id, updateDTO, perfilLogado);

        return ResponseEntity.ok("As informações do grupo '" + updateDTO.titulo() + "' foram atualizadas com sucesso!");
    }

    @GetMapping("/pesquisar")
    public ResponseEntity<?> pesquisar(BuscaGrupoTeamaticoDTO filtro) {
        try {
            List<GrupoTematico> grupos = grupoTematicoService.buscarGrupos(filtro);

            if (grupos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum grupo encontrado");
            }

            return ResponseEntity.ok(grupos);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro inesperado: Falha ao realizar a pesquisa.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> excluirGrupo(
            @PathVariable Long id,
            @AuthenticationPrincipal Perfil perfilLogado) {
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
