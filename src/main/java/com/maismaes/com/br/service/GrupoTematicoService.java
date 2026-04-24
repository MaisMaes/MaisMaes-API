package com.maismaes.com.br.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import org.springframework.stereotype.Service;

import com.maismaes.com.br.dto.request.BuscaGrupoTeamaticoDTO;
import com.maismaes.com.br.dto.request.EditarGrupoTematicoRequestDTO;
import com.maismaes.com.br.dto.response.ListarGrupoTematicoDTO;
import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.entities.Usuario;
import com.maismaes.com.br.entities.grupo_tematico.Bairro;
import com.maismaes.com.br.entities.grupo_tematico.Categoria;
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
    public GrupoTematico criarGrupoTematico(GrupoTematico grupo, List<String> nomesBairros, Perfil perfilLogado) {
        Usuario criadora = perfilLogado.getUsuario();
        grupo.setCriador(criadora);

        ParticipanteGrupo vinculoAdm = ParticipanteGrupo.builder()
                .grupo(grupo)
                .role(GrupoRole.CRIADORA)
                .usuario(criadora)
                .dataAdesao(LocalDateTime.now())
                .build();
        
        grupo.getParticipantes().add(vinculoAdm);

        // Transformar List<String> em List<Bairro> vinculando ao grupo
        List<Bairro> bairrosEntities = nomesBairros.stream()
                .map(nome -> Bairro.builder()
                        .nome(nome)
                        .grupo(grupo)
                        .build())
                .toList();

        grupo.setBairros(bairrosEntities);

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

    @Transactional
    public GrupoTematico atualizarGrupo(Long grupoId, EditarGrupoTematicoRequestDTO dto, Perfil perfilLogado) {
        
        ParticipanteGrupo executor = participanteGrupoRepository
                .findByGrupoIdAndUsuarioId(grupoId, perfilLogado.getUsuario().getId())
                .orElseThrow(() -> new RuntimeException("Você não faz parte deste grupo"));

        // Apenas criadora e moderadora podem editar(APLICAÇÃO DA ROLE DO GRUPO)
        if (executor.getRole() == GrupoRole.PARTICIPANTE) {
            throw new RuntimeException("Ação negada: Apenas moderadoras ou a criadora podem editar o grupo.");
        }

        // Busca o grupo no banco
        GrupoTematico grupo = grupoTematicoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado"));

        grupo.setTitulo(dto.titulo());
        grupo.setDescricao(dto.descricao());
        grupo.setCategorias(Categoria.valueOf(dto.categorias().toUpperCase()));
        grupo.setPrivado(dto.privado());
        grupo.setNumeroParticipantes(dto.numeroParticipantes());
        grupo.setTempoEntreMensagens(dto.tempoEntreMensagens());
        grupo.setVideo(dto.video());
        grupo.setAudio(dto.audio());
        grupo.setImagem(dto.imagem());
        grupo.setDocumento(dto.documento());

        //Parte da edição dos bairros
        List<String> bairrosAtuais = grupo.getBairros().stream()
            .map(Bairro::getNome)
            .toList();

        if (dto.bairros() != null && !dto.bairros().isEmpty() && !bairrosAtuais.equals(dto.bairros())) {

            grupo.getBairros().clear();
            
            List<Bairro> novosBairros = dto.bairros().stream()
                .<Bairro>map(nome -> Bairro.builder()
                        .nome(nome)
                        .grupo(grupo)
                        .build())
                .toList();

            grupo.getBairros().addAll(novosBairros);
        }  

        return grupoTematicoRepository.save(grupo);
    }

    public List<ListarGrupoTematicoDTO> listarTodos() {
        return grupoTematicoRepository.findAll()
                .stream()
                .map(ListarGrupoTematicoDTO::new)
                .toList();
    }

    public List<GrupoTematico> buscarGrupos(BuscaGrupoTeamaticoDTO filtro) {
        return grupoTematicoRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro.id() != null) {
                predicates.add((Predicate) cb.equal(root.get("id"), filtro.id()));
            }

            if (filtro.titulo() != null && !filtro.titulo().isBlank()) {
                predicates.add(
                        (Predicate) cb.like(cb.lower(root.get("titulo")), "%" + filtro.titulo().toLowerCase() + "%"));
            }

            if (filtro.categorias() != null && !filtro.categorias().isBlank()) {
                predicates.add((Predicate) cb.equal(root.get("categorias"),
                        Categoria.valueOf(filtro.categorias().toUpperCase())));
            }

            if (filtro.bairro() != null && !filtro.bairro().isBlank()) {
                predicates.add(
                        (Predicate) cb.like(cb.lower(root.get("bairro")), "%" + filtro.bairro().toLowerCase() + "%"));
            }

            return cb.and((jakarta.persistence.criteria.Predicate[]) predicates.toArray(new Predicate[0]));
        });
    }

    @Transactional
    public void excluirGrupo(Long grupoId, Perfil perfilLogado) {

        ParticipanteGrupo executor = participanteGrupoRepository
                .findByGrupoIdAndUsuarioId(grupoId, perfilLogado.getUsuario().getId())
                .orElseThrow(() -> new RuntimeException("Você não tem permissão para acessar este grupo."));

        if (executor.getRole() != GrupoRole.CRIADORA) {
            throw new RuntimeException("Ação negada: Apenas a criadora original pode excluir o grupo.");
        }

        GrupoTematico grupo = grupoTematicoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado."));

        grupoTematicoRepository.delete(grupo);
    }

}
