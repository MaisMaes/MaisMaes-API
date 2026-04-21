package com.maismaes.com.br.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.entities.Usuario;
import com.maismaes.com.br.entities.grupo_tematico.GrupoRole;
import com.maismaes.com.br.entities.grupo_tematico.GrupoTematico;
import com.maismaes.com.br.entities.grupo_tematico.ParticipanteGrupo;
import com.maismaes.com.br.repository.GrupoTematicoRepository;
import com.maismaes.com.br.repository.ParticipanteGrupoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class GrupoTematicoService {

    private final GrupoTematicoRepository grupoTematicoRepository;
    private final ParticipanteGrupoRepository participanteGrupoRepository;

    @Transactional
    public GrupoTematico criarGrupoTematico(GrupoTematico grupo, Perfil perfilLogado) {
    
        Usuario criadora = perfilLogado.getUsuario();
        grupo.setCriador(criadora);
        
        //ADM vinculada na criação
        ParticipanteGrupo vinculoAdm = ParticipanteGrupo.builder()
                .grupo(grupo)
                .role(GrupoRole.CRIADORA)
                .usuario(criadora)
                .dataAdesao(LocalDateTime.now())
                .build();
        
        grupo.getParticipantes().add(vinculoAdm);
        
        return grupoTematicoRepository.save(grupo);
    }

    @Transactional
    public void alterarPrivilegio(Long grupoId, UUID usuarioAlvoId, GrupoRole novoCargo, Perfil perfilLogado) {
        
        Usuario perfilExecutor = perfilLogado.getUsuario();

        // System.out.println("Perfil Executor: " + perfilExecutor.getId());

        ParticipanteGrupo executor = participanteGrupoRepository
            .findByGrupoIdAndUsuarioId(grupoId, perfilExecutor.getId())
            .orElseThrow(() -> new RuntimeException("Você não faz parte deste grupo"));

        if (executor.getRole() != GrupoRole.CRIADORA) {
            throw new RuntimeException("Ação negada: Apenas a dona do grupo pode gerenciar moderadores.");
        }

        ParticipanteGrupo alvo = participanteGrupoRepository
            .findByGrupoIdAndUsuarioId(grupoId, usuarioAlvoId)
            .orElseThrow(() -> new RuntimeException("Usuária alvo não encontrada neste grupo."));

        if (alvo.getUsuario().getId().equals(perfilExecutor.getId()) && novoCargo != GrupoRole.CRIADORA) {
        throw new RuntimeException("A criadora não pode abdicar do seu cargo desta forma.");
        }
        
        alvo.setRole(novoCargo);
    }

}
