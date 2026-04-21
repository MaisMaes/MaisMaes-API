package com.maismaes.com.br.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.maismaes.com.br.dto.request.GrupoTematicoRequestDTO;
import com.maismaes.com.br.dto.response.GrupoTematicoResponseDTO;
import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.entities.grupo_tematico.GrupoRole;
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
        @AuthenticationPrincipal Perfil perfilLogado //Verifica pelo spring se o usuário está autenticado e passa o perfil para o método
        ){
    
            var grupoCriado = grupoTematicoService.criarGrupoTematico(
                grupoTematicoRequestDTO.ToGrupoTematicoEntity(), 
                perfilLogado
            );

            return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(new GrupoTematicoResponseDTO(grupoCriado.getId()));
    }

    @PatchMapping("/{grupoId}/membros/{usuarioId}/privilegio")
    public ResponseEntity<String> mudarPrivilegio(
        @PathVariable Long grupoId,
        @PathVariable UUID usuarioId,
        @RequestParam GrupoRole novaRole,
        @AuthenticationPrincipal Perfil perfilLogado
    ) {
        grupoTematicoService.alterarPrivilegio(grupoId, usuarioId, novaRole, perfilLogado);
        return ResponseEntity.ok("Privilégio atualizado com sucesso!");
    }

}
