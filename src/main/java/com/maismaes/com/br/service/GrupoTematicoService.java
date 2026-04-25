package com.maismaes.com.br.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.maismaes.com.br.dto.request.EditarGrupoTematicoRequestDTO;
import com.maismaes.com.br.dto.response.DetalheGrupoResponseDTO;
import com.maismaes.com.br.dto.response.ListarGrupoTematicoDTO;
import com.maismaes.com.br.dto.response.ParticipanteGrupoResumoResponseDTO;
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

    // @Transactional
    // public void alterarPrivilegio(Long grupoId, UUID usuarioAlvoId, GrupoRole novoCargo, Perfil perfilLogado) {

    //     Usuario perfilExecutor = perfilLogado.getUsuario();

    //     // System.out.println("Perfil Executor: " + perfilExecutor.getId());

    //     ParticipanteGrupo executor = participanteGrupoRepository
    //             .findByGrupoIdAndUsuarioId(grupoId, perfilExecutor.getId())
    //             .orElseThrow(() -> new RuntimeException("Você não faz parte deste grupo"));

    //     if (executor.getRole() != GrupoRole.CRIADORA) {
    //         throw new RuntimeException("Ação negada: Apenas a dona do grupo pode gerenciar moderadores.");
    //     }

    //     ParticipanteGrupo alvo = participanteGrupoRepository
    //             .findByGrupoIdAndUsuarioId(grupoId, usuarioAlvoId)
    //             .orElseThrow(() -> new RuntimeException("Usuária alvo não encontrada neste grupo."));

    //     if (alvo.getUsuario().getId().equals(perfilExecutor.getId()) && novoCargo != GrupoRole.CRIADORA) {
    //         throw new RuntimeException("A criadora não pode abdicar do seu cargo desta forma.");
    //     }

    //     alvo.setRole(novoCargo);
    // }

    // Editar grupo
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

    //Listar todos os grupos
    public List<ListarGrupoTematicoDTO> listarTodos() {
        return grupoTematicoRepository.findAll()
                .stream()
                .map(ListarGrupoTematicoDTO::new)
                .toList();
    }

    //Obter grupo especifico - detalhe do grupo
    @Transactional 
    public DetalheGrupoResponseDTO obterDetalhes(Long grupoId, UUID usuarioLogadoId) {
        
        GrupoTematico grupo = grupoTematicoRepository.findById(grupoId)
            .orElseThrow(() -> new RuntimeException("Grupo não encontrado"));

        List<String> nomesBairros = grupo.getBairros().stream()
            .map(Bairro::getNome) 
            .toList();

        List<ParticipanteGrupoResumoResponseDTO> participantes = grupo.getParticipantes().stream()
            .map(p -> new ParticipanteGrupoResumoResponseDTO(
                p.getUsuario().getId(), 
                p.getUsuario().getNome(),
                p.getRole().name()
            ))
            .toList();

        boolean isParticipante = grupo.getParticipantes().stream()
            .anyMatch(p -> p.getUsuario().getId().equals(usuarioLogadoId));

        return new DetalheGrupoResponseDTO(
            grupo.getId(),
            grupo.getTitulo(),
            grupo.getDescricao(),
            grupo.getCategorias().name(),
            grupo.isPrivado(),
            grupo.getNumeroParticipantes(),
            grupo.getTempoEntreMensagens(),
            grupo.isVideo(),
            grupo.isAudio(),
            grupo.isImagem(),
            grupo.isDocumento(),
            nomesBairros,
            participantes,
            isParticipante
        );
    }

    //Pesquisa global sem filtros
    public List<ListarGrupoTematicoDTO> pesquisarGrupoTematico(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            throw new RuntimeException("Nenhum grupo encontrado");
        }

        String busca = termo.trim();
        Set<GrupoTematico> resultados = new HashSet<>();

        try {

            if (busca.matches("\\d+")) { 
                grupoTematicoRepository.findById(Long.parseLong(busca)).ifPresent(resultados::add);
            }

            try {
                Categoria categoria = Categoria.valueOf(busca.toUpperCase());
                resultados.addAll(grupoTematicoRepository.findByCategorias(categoria));
            } catch (IllegalArgumentException e) {

            }

            resultados.addAll(grupoTematicoRepository.findByTituloContainingIgnoreCase(busca));

            resultados.addAll(grupoTematicoRepository.buscarPorNomeBairro(busca));

            if (resultados.isEmpty()) {
                throw new RuntimeException("Nenhum grupo encontrado");
            }

            return resultados.stream()
                    .map(ListarGrupoTematicoDTO::new)
                    .toList();

        } catch (RuntimeException e) {
            throw e; 
        } catch (Exception e) {
            throw new RuntimeException("Erro inesperado ao realizar a pesquisa");
        }
    }

    //excluir
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
