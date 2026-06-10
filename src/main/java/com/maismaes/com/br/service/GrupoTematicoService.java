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
import com.maismaes.com.br.entities.grupo_tematico.FavoritoGrupo;
import com.maismaes.com.br.entities.grupo_tematico.GrupoRole;
import com.maismaes.com.br.entities.grupo_tematico.GrupoTematico;
import com.maismaes.com.br.entities.grupo_tematico.ParticipanteGrupo;
import com.maismaes.com.br.repository.FavoritoGrupoRepository;
import com.maismaes.com.br.repository.GrupoTematicoRepository;
import com.maismaes.com.br.repository.ParticipanteGrupoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class GrupoTematicoService {

  private final GrupoTematicoRepository grupoTematicoRepository;
  private final ParticipanteGrupoRepository participanteGrupoRepository;
  private final FavoritoGrupoRepository favoritoGrupoRepository;

  @Transactional
  public GrupoTematico criarGrupoTematico(
      GrupoTematico grupo, List<String> nomesBairros, Perfil perfilLogado) {
    Usuario criadora = perfilLogado.getUsuario();
    grupo.setCriador(criadora);

    ParticipanteGrupo vinculoAdm =
        ParticipanteGrupo.builder()
            .grupo(grupo)
            .role(GrupoRole.CRIADORA)
            .usuario(criadora)
            .dataAdesao(LocalDateTime.now())
            .build();

    grupo.getParticipantes().add(vinculoAdm);

    // Transformar List<String> em List<Bairro> vinculando ao grupo
    List<Bairro> bairrosEntities =
        nomesBairros.stream()
            .map(nome -> Bairro.builder().nome(nome).grupo(grupo).build())
            .toList();

    grupo.setBairros(bairrosEntities);

    return grupoTematicoRepository.save(grupo);
  }

  // Editar grupo
  @Transactional
  public GrupoTematico atualizarGrupo(
      Long grupoId, EditarGrupoTematicoRequestDTO dto, Perfil perfilLogado) {

    ParticipanteGrupo executor =
        participanteGrupoRepository
            .findByGrupoIdAndUsuarioId(grupoId, perfilLogado.getUsuario().getId())
            .orElseThrow(() -> new RuntimeException("Você não faz parte deste grupo"));

    // Apenas criadora e moderadora podem editar(APLICAÇÃO DA ROLE DO GRUPO)
    if (executor.getRole() == GrupoRole.PARTICIPANTE) {
      throw new RuntimeException(
          "Ação negada: Apenas moderadoras ou a criadora podem editar o grupo.");
    }

    // Busca o grupo no banco
    GrupoTematico grupo =
        grupoTematicoRepository
            .findById(grupoId)
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

    // Parte da edição dos bairros
    List<String> bairrosAtuais = grupo.getBairros().stream().map(Bairro::getNome).toList();

    if (dto.bairros() != null && !dto.bairros().isEmpty() && !bairrosAtuais.equals(dto.bairros())) {

      grupo.getBairros().clear();

      List<Bairro> novosBairros =
          dto.bairros().stream()
              .<Bairro>map(nome -> Bairro.builder().nome(nome).grupo(grupo).build())
              .toList();

      grupo.getBairros().addAll(novosBairros);
    }

    return grupoTematicoRepository.save(grupo);
  }

  // Entrar em um grupo
  @Transactional
  public void entrarNoGrupo(Long grupoId, Perfil perfilLogado) {
    GrupoTematico grupo =
        grupoTematicoRepository
            .findById(grupoId)
            .orElseThrow(() -> new RuntimeException("Grupo não encontrado."));

    Usuario usuario = perfilLogado.getUsuario();

    if (participanteGrupoRepository.existsByGrupoIdAndUsuarioId(grupoId, usuario.getId())) {
      throw new RuntimeException("Você já é participante deste grupo.");
    }

    int lotacaoMaxima = grupo.getNumeroParticipantes();
    int participantesAtuais = grupo.getParticipantes().size();
    if (participantesAtuais >= lotacaoMaxima) {
      throw new RuntimeException("Grupo lotado: número máximo de participantes atingido.");
    }

    ParticipanteGrupo vinculo =
        ParticipanteGrupo.builder()
            .grupo(grupo)
            .usuario(usuario)
            .role(GrupoRole.PARTICIPANTE)
            .dataAdesao(LocalDateTime.now())
            .build();

    participanteGrupoRepository.save(vinculo);
  }

  // Listar grupos que o usuário participa
  public List<ListarGrupoTematicoDTO> listarGruposDoUsuario(Perfil perfilLogado) {
    return participanteGrupoRepository.findByUsuarioId(perfilLogado.getUsuario().getId()).stream()
        .map(ParticipanteGrupo::getGrupo)
        .map(ListarGrupoTematicoDTO::new)
        .toList();
  }

  @Transactional
  public void favoritarGrupo(Long grupoId, Perfil perfilLogado) {
    Usuario usuario = perfilLogado.getUsuario();

    if (favoritoGrupoRepository.existsByGrupoIdAndUsuarioId(grupoId, usuario.getId())) {
      return;
    }

    GrupoTematico grupo =
        grupoTematicoRepository
            .findById(grupoId)
            .orElseThrow(() -> new RuntimeException("Grupo nao encontrado."));

    FavoritoGrupo favorito =
        FavoritoGrupo.builder()
            .grupo(grupo)
            .usuario(usuario)
            .dataFavorito(LocalDateTime.now())
            .build();

    favoritoGrupoRepository.save(favorito);
  }

  @Transactional
  public void removerFavoritoGrupo(Long grupoId, Perfil perfilLogado) {
    if (!grupoTematicoRepository.existsById(grupoId)) {
      throw new RuntimeException("Grupo nao encontrado.");
    }

    favoritoGrupoRepository.deleteByGrupoIdAndUsuarioId(grupoId, perfilLogado.getUsuario().getId());
  }

  public List<ListarGrupoTematicoDTO> listarGruposFavoritos(Perfil perfilLogado) {
    return favoritoGrupoRepository.findByUsuarioId(perfilLogado.getUsuario().getId()).stream()
        .map(FavoritoGrupo::getGrupo)
        .map(ListarGrupoTematicoDTO::new)
        .toList();
  }

  public List<ListarGrupoTematicoDTO> listarTodos() {
    return grupoTematicoRepository.findAll().stream().map(ListarGrupoTematicoDTO::new).toList();
  }

  // Obter grupo especifico - detalhe do grupo
  @Transactional
  public DetalheGrupoResponseDTO obterDetalhes(Long grupoId, UUID usuarioLogadoId) {

    GrupoTematico grupo =
        grupoTematicoRepository
            .findById(grupoId)
            .orElseThrow(() -> new RuntimeException("Grupo não encontrado"));

    List<String> nomesBairros = grupo.getBairros().stream().map(Bairro::getNome).toList();

    List<ParticipanteGrupoResumoResponseDTO> participantes =
        grupo.getParticipantes().stream()
            .map(
                p ->
                    new ParticipanteGrupoResumoResponseDTO(
                        p.getUsuario().getId(), p.getUsuario().getNome(), p.getRole().name()))
            .toList();

    var participanteLogado =
        grupo.getParticipantes().stream()
            .filter(p -> p.getUsuario().getId().equals(usuarioLogadoId))
            .findFirst();

    boolean isParticipante = participanteLogado.isPresent();

    String roleLogada = participanteLogado.map(p -> p.getRole().name()).orElse(null);

    boolean usuarioLogadoFavoritou =
        favoritoGrupoRepository.existsByGrupoIdAndUsuarioId(grupoId, usuarioLogadoId);

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
        isParticipante,
        roleLogada,
        usuarioLogadoFavoritou);
  }

  // Pesquisa global sem filtros
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

      return resultados.stream().map(ListarGrupoTematicoDTO::new).toList();

    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Erro inesperado ao realizar a pesquisa");
    }
  }

  // excluir
  @Transactional
  public void excluirGrupo(Long grupoId, Perfil perfilLogado) {

    ParticipanteGrupo executor =
        participanteGrupoRepository
            .findByGrupoIdAndUsuarioId(grupoId, perfilLogado.getUsuario().getId())
            .orElseThrow(
                () -> new RuntimeException("Você não tem permissão para acessar este grupo."));

    if (executor.getRole() != GrupoRole.CRIADORA) {
      throw new RuntimeException("Ação negada: Apenas a criadora original pode excluir o grupo.");
    }

    GrupoTematico grupo =
        grupoTematicoRepository
            .findById(grupoId)
            .orElseThrow(() -> new RuntimeException("Grupo não encontrado."));

    grupoTematicoRepository.delete(grupo);
  }
}
