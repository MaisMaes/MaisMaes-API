package com.maismaes.com.br.service;

import com.maismaes.com.br.dto.request.AtualizarDenunciaDTO;
import com.maismaes.com.br.dto.request.DenunciaGrupoFilterDTO;
import com.maismaes.com.br.dto.request.DenunciaGrupoResponseDTO;
import com.maismaes.com.br.dto.request.EditarGrupoTematicoRequestDTO;
import com.maismaes.com.br.dto.response.DetalheGrupoResponseDTO;
import com.maismaes.com.br.dto.response.ListarGrupoTematicoDTO;
import com.maismaes.com.br.dto.response.MembroStatusResponseDTO;
import com.maismaes.com.br.dto.response.ParticipanteGrupoResumoResponseDTO;
import com.maismaes.com.br.dto.response.PedidoEntradaResponseDTO;
import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.entities.Usuario;
import com.maismaes.com.br.entities.grupo_tematico.*;
import com.maismaes.com.br.repository.DenunciarGrupoRepository;
import com.maismaes.com.br.repository.FavoritoGrupoRepository;
import com.maismaes.com.br.repository.GrupoTematicoRepository;
import com.maismaes.com.br.repository.ParticipanteGrupoRepository;
import com.maismaes.com.br.repository.PedidoEntradaGrupoRepository;
import com.maismaes.com.br.utils.DenunciarGrupoSpecification;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class GrupoTematicoService {

  @Value("${denuncia.limiar-notificacao:1}")
  private int LIMIAR_NOTIFICACAO_DENUNCIAS;

  @Value("${admin.email:alissongabriel010907@gmail.com}")
  private String adminEmail;

  private final GrupoTematicoRepository grupoTematicoRepository;
  private final ParticipanteGrupoRepository participanteGrupoRepository;
  private final FavoritoGrupoRepository favoritoGrupoRepository;
  private final DenunciarGrupoRepository denunciarGrupoRepository;
  private final PedidoEntradaGrupoRepository pedidoEntradaGrupoRepository;
  private final EmailService emailService;

  @Transactional
  public GrupoTematico criarGrupoTematico(
      GrupoTematico grupo, List<String> nomesBairros, Perfil perfilLogado) {
    log.info(
        "[GrupoTematicoService] criarGrupoTematico - Iniciando criação de grupo para usuário: {}",
        perfilLogado.getUsuario().getId());

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

    List<Bairro> bairrosEntities =
        nomesBairros.stream()
            .map(nome -> Bairro.builder().nome(nome).grupo(grupo).build())
            .toList();

    grupo.setBairros(bairrosEntities);

    GrupoTematico salvo = grupoTematicoRepository.save(grupo);
    log.info(
        "[GrupoTematicoService] criarGrupoTematico - Grupo criado com sucesso. ID: {}",
        salvo.getId());
    return salvo;
  }

  // Editar grupo
  @Transactional
  public GrupoTematico atualizarGrupo(
      Long grupoId, EditarGrupoTematicoRequestDTO dto, Perfil perfilLogado) {
    log.info(
        "[GrupoTematicoService] atualizarGrupo - Usuário {} tentando editar grupo ID: {}",
        perfilLogado.getUsuario().getId(),
        grupoId);

    ParticipanteGrupo executor =
        participanteGrupoRepository
            .findByGrupoIdAndUsuarioId(grupoId, perfilLogado.getUsuario().getId())
            .orElseThrow(() -> new RuntimeException("Você não faz parte deste grupo"));

    if (executor.getRole() == GrupoRole.PARTICIPANTE) {
      log.warn(
          "[GrupoTematicoService] atualizarGrupo - Usuário {} sem permissão para editar grupo {}",
          perfilLogado.getUsuario().getId(),
          grupoId);
      throw new RuntimeException(
          "Ação negada: Apenas moderadoras ou a criadora podem editar o grupo.");
    }

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

    List<String> bairrosAtuais = grupo.getBairros().stream().map(Bairro::getNome).toList();

    if (dto.bairros() != null && !dto.bairros().isEmpty() && !bairrosAtuais.equals(dto.bairros())) {
      log.info(
          "[GrupoTematicoService] atualizarGrupo - Atualizando bairros do grupo {}", grupoId);
      grupo.getBairros().clear();

      List<Bairro> novosBairros =
          dto.bairros().stream()
              .<Bairro>map(nome -> Bairro.builder().nome(nome).grupo(grupo).build())
              .toList();

      grupo.getBairros().addAll(novosBairros);
    }

    GrupoTematico atualizado = grupoTematicoRepository.save(grupo);
    log.info(
        "[GrupoTematicoService] atualizarGrupo - Grupo {} atualizado com sucesso", grupoId);
    return atualizado;
  }

  // Entrar em um grupo (ou solicitar entrada se privado)
  @Transactional
  public String entrarNoGrupo(Long grupoId, Perfil perfilLogado) {
    log.info(
        "[GrupoTematicoService] entrarNoGrupo - Usuário {} tentando entrar no grupo {}",
        perfilLogado.getUsuario().getId(),
        grupoId);

    GrupoTematico grupo =
        grupoTematicoRepository
            .findById(grupoId)
            .orElseThrow(() -> new RuntimeException("Grupo não encontrado."));

    Usuario usuario = perfilLogado.getUsuario();

    Optional<ParticipanteGrupo> participante =
        participanteGrupoRepository.findByGrupoIdAndUsuarioId(grupoId, usuario.getId());

    if (participante.isPresent()) {
      String motivo = participante.get().getMotivoBanimento();
      if (motivo != null && !motivo.isBlank()) {
        log.warn(
            "[GrupoTematicoService] entrarNoGrupo - Usuário {} está banido do grupo {}. Motivo: {}",
            usuario.getId(),
            grupoId,
            motivo);
        throw new RuntimeException("Você foi banido deste grupo. Motivo: " + motivo);
      }
      log.warn(
          "[GrupoTematicoService] entrarNoGrupo - Usuário {} já é participante do grupo {}",
          usuario.getId(),
          grupoId);
      throw new RuntimeException("Você já é participante deste grupo.");
    }

    // Grupo privado → cria pedido de entrada
    if (grupo.isPrivado()) {
      if (pedidoEntradaGrupoRepository.existsByGrupoIdAndUsuarioId(grupoId, usuario.getId())) {
        log.warn(
            "[GrupoTematicoService] entrarNoGrupo - Usuário {} já possui pedido pendente para o grupo {}",
            usuario.getId(),
            grupoId);
        throw new RuntimeException(
            "Você já possui um pedido de entrada pendente para este grupo.");
      }
      PedidoEntradaGrupo pedido =
          PedidoEntradaGrupo.builder().grupo(grupo).usuario(usuario).build();
      pedidoEntradaGrupoRepository.save(pedido);
      log.info(
          "[GrupoTematicoService] entrarNoGrupo - Pedido de entrada criado para usuário {} no grupo privado {}",
          usuario.getId(),
          grupoId);
      return "Grupo privado: seu pedido de entrada foi enviado e aguarda aprovação da criadora.";
    }

    // Grupo público → entra diretamente
    int lotacaoMaxima = grupo.getNumeroParticipantes();
    int participantesAtuais = grupo.getParticipantes().size();
    log.info(
        "[GrupoTematicoService] entrarNoGrupo - Grupo {}: {}/{} participantes",
        grupoId,
        participantesAtuais,
        lotacaoMaxima);

    if (participantesAtuais >= lotacaoMaxima) {
      log.warn(
          "[GrupoTematicoService] entrarNoGrupo - Grupo {} está lotado ({}/{})",
          grupoId,
          participantesAtuais,
          lotacaoMaxima);
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
    log.info(
        "[GrupoTematicoService] entrarNoGrupo - Usuário {} entrou no grupo {} com sucesso",
        usuario.getNome(),
        grupoId);

    emailService.notificarNovoParticipante(
        grupo.getCriador().getEmail(), grupo.getTitulo(), usuario.getNome());

    return "Você entrou no grupo com sucesso!";
  }

  // Listar pedidos de entrada pendentes de um grupo (somente criadora/moderadora)
  public List<PedidoEntradaResponseDTO> listarPedidosPendentes(
      Long grupoId, Perfil perfilLogado) {
    log.info(
        "[GrupoTematicoService] listarPedidosPendentes - Usuário {} listando pedidos do grupo {}",
        perfilLogado.getUsuario().getId(),
        grupoId);

    ParticipanteGrupo executor =
        participanteGrupoRepository
            .findByGrupoIdAndUsuarioId(grupoId, perfilLogado.getUsuario().getId())
            .orElseThrow(() -> new RuntimeException("Você não faz parte deste grupo."));

    if (executor.getRole() != GrupoRole.CRIADORA && executor.getRole() != GrupoRole.MODERADORA) {
      throw new RuntimeException(
          "Ação negada: apenas a criadora ou moderadora podem ver os pedidos de entrada.");
    }

    List<PedidoEntradaResponseDTO> pedidos =
        pedidoEntradaGrupoRepository
            .findByGrupoIdAndStatus(grupoId, StatusPedidoEntrada.PENDENTE)
            .stream()
            .map(PedidoEntradaResponseDTO::new)
            .toList();

    log.info(
        "[GrupoTematicoService] listarPedidosPendentes - {} pedido(s) pendente(s) no grupo {}",
        pedidos.size(),
        grupoId);
    return pedidos;
  }

  // Aprovar ou rejeitar pedido de entrada (somente criadora/moderadora)
  @Transactional
  public PedidoEntradaResponseDTO responderPedido(
      Long grupoId, Long pedidoId, boolean aprovado, Perfil perfilLogado) {
    log.info(
        "[GrupoTematicoService] responderPedido - Usuário {} {} pedido {} do grupo {}",
        perfilLogado.getUsuario().getId(),
        aprovado ? "aprovando" : "rejeitando",
        pedidoId,
        grupoId);

    ParticipanteGrupo executor =
        participanteGrupoRepository
            .findByGrupoIdAndUsuarioId(grupoId, perfilLogado.getUsuario().getId())
            .orElseThrow(() -> new RuntimeException("Você não faz parte deste grupo."));

    if (executor.getRole() != GrupoRole.CRIADORA && executor.getRole() != GrupoRole.MODERADORA) {
      throw new RuntimeException(
          "Ação negada: apenas a criadora ou moderadora podem responder pedidos de entrada.");
    }

    PedidoEntradaGrupo pedido =
        pedidoEntradaGrupoRepository
            .findById(pedidoId)
            .orElseThrow(() -> new RuntimeException("Pedido de entrada não encontrado."));

    if (!pedido.getGrupo().getId().equals(grupoId)) {
      throw new RuntimeException("Este pedido não pertence ao grupo informado.");
    }

    if (pedido.getStatus() != StatusPedidoEntrada.PENDENTE) {
      throw new RuntimeException(
          "Este pedido já foi respondido. Status atual: " + pedido.getStatus().name());
    }

    pedido.setStatus(aprovado ? StatusPedidoEntrada.APROVADO : StatusPedidoEntrada.REJEITADO);
    pedido.setDataResposta(LocalDateTime.now());
    pedidoEntradaGrupoRepository.save(pedido);

    if (aprovado) {
      GrupoTematico grupo = pedido.getGrupo();
      int lotacaoMaxima = grupo.getNumeroParticipantes();
      int participantesAtuais = grupo.getParticipantes().size();

      if (participantesAtuais >= lotacaoMaxima) {
        log.warn(
            "[GrupoTematicoService] responderPedido - Grupo {} lotado ao tentar aprovar pedido {}",
            grupoId,
            pedidoId);
        throw new RuntimeException(
            "Não foi possível aprovar: grupo lotado ("
                + participantesAtuais
                + "/"
                + lotacaoMaxima
                + ").");
      }

      ParticipanteGrupo vinculo =
          ParticipanteGrupo.builder()
              .grupo(grupo)
              .usuario(pedido.getUsuario())
              .role(GrupoRole.PARTICIPANTE)
              .dataAdesao(LocalDateTime.now())
              .build();

      participanteGrupoRepository.save(vinculo);
      log.info(
          "[GrupoTematicoService] responderPedido - Pedido {} aprovado. Usuário {} adicionado ao grupo {}",
          pedidoId,
          pedido.getUsuario().getId(),
          grupoId);

      emailService.notificarNovoParticipante(
          grupo.getCriador().getEmail(), grupo.getTitulo(), pedido.getUsuario().getNome());
    } else {
      log.info(
          "[GrupoTematicoService] responderPedido - Pedido {} rejeitado para usuário {} no grupo {}",
          pedidoId,
          pedido.getUsuario().getId(),
          grupoId);
    }

    return new PedidoEntradaResponseDTO(pedido);
  }

  // Listar grupos que o usuário participa
  public List<ListarGrupoTematicoDTO> listarGruposDoUsuario(Perfil perfilLogado) {
    log.info(
        "[GrupoTematicoService] listarGruposDoUsuario - Listando grupos do usuário: {}",
        perfilLogado.getUsuario().getNome());
    List<ListarGrupoTematicoDTO> grupos =
        participanteGrupoRepository.findByUsuarioId(perfilLogado.getUsuario().getId()).stream()
            .map(ParticipanteGrupo::getGrupo)
            .map(ListarGrupoTematicoDTO::new)
            .toList();
    log.info(
        "[GrupoTematicoService] listarGruposDoUsuario - {} grupo(s) encontrado(s) para o usuário {}",
        grupos.size(),
        perfilLogado.getUsuario().getNome());
    return grupos;
  }

  @Transactional
  public void favoritarGrupo(Long grupoId, Perfil perfilLogado) {
    log.info(
        "[GrupoTematicoService] favoritarGrupo - Usuário {} favoritando grupo {}",
        perfilLogado.getUsuario().getNome(),
        grupoId);

    Usuario usuario = perfilLogado.getUsuario();

    if (favoritoGrupoRepository.existsByGrupoIdAndUsuarioId(grupoId, usuario.getId())) {
      log.info(
          "[GrupoTematicoService] favoritarGrupo - Grupo {} já está favoritado pelo usuário {}",
          grupoId,
          usuario.getNome());
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
    log.info(
        "[GrupoTematicoService] favoritarGrupo - Grupo {} favoritado com sucesso pelo usuário {}",
        grupoId,
        usuario.getNome());
  }

  @Transactional
  public void removerFavoritoGrupo(Long grupoId, Perfil perfilLogado) {
    log.info(
        "[GrupoTematicoService] removerFavoritoGrupo - Usuário {} removendo favorito do grupo {}",
        perfilLogado.getUsuario().getNome(),
        grupoId);

    if (!grupoTematicoRepository.existsById(grupoId)) {
      throw new RuntimeException("Grupo nao encontrado.");
    }

    favoritoGrupoRepository.deleteByGrupoIdAndUsuarioId(grupoId, perfilLogado.getUsuario().getId());
    log.info(
        "[GrupoTematicoService] removerFavoritoGrupo - Favorito removido com sucesso. Grupo: {}, Usuário: {}",
        grupoId,
        perfilLogado.getUsuario().getNome());
  }

  public List<ListarGrupoTematicoDTO> listarGruposFavoritos(Perfil perfilLogado) {
    log.info(
        "[GrupoTematicoService] listarGruposFavoritos - Listando favoritos do usuário: {}",
        perfilLogado.getUsuario().getNome());
    List<ListarGrupoTematicoDTO> favoritos =
        favoritoGrupoRepository.findByUsuarioId(perfilLogado.getUsuario().getId()).stream()
            .map(FavoritoGrupo::getGrupo)
            .map(ListarGrupoTematicoDTO::new)
            .toList();
    log.info(
        "[GrupoTematicoService] listarGruposFavoritos - {} favorito(s) encontrado(s) para o usuário {}",
        favoritos.size(),
        perfilLogado.getUsuario().getNome());
    return favoritos;
  }

  public List<ListarGrupoTematicoDTO> listarTodos() {
    log.info("[GrupoTematicoService] listarTodos - Listando todos os grupos");
    List<ListarGrupoTematicoDTO> grupos =
        grupoTematicoRepository.findAll().stream().map(ListarGrupoTematicoDTO::new).toList();
    log.info("[GrupoTematicoService] listarTodos - {} grupo(s) encontrado(s)", grupos.size());
    return grupos;
  }

  // Obter grupo especifico - detalhe do grupo
  @Transactional
  public DetalheGrupoResponseDTO obterDetalhes(Long grupoId, UUID usuarioLogadoId) {
    log.info(
        "[GrupoTematicoService] obterDetalhes - Buscando detalhes do grupo {}",
        grupoId);

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

    boolean aguardandoAprovacao =
        pedidoEntradaGrupoRepository
            .findByGrupoIdAndUsuarioId(grupoId, usuarioLogadoId)
            .map(p -> p.getStatus() == StatusPedidoEntrada.PENDENTE)
            .orElse(false);

    log.info(
        "[GrupoTematicoService] obterDetalhes - Detalhes do grupo {} retornados. Participante: {}, Role: {}, AguardandoAprovacao: {}",
        grupoId,
        isParticipante,
        roleLogada,
        aguardandoAprovacao);

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
        usuarioLogadoFavoritou,
        aguardandoAprovacao);
  }

  // Pesquisa global sem filtros
  public List<ListarGrupoTematicoDTO> pesquisarGrupoTematico(String termo) {
    log.info("[GrupoTematicoService] pesquisarGrupoTematico - Termo de busca: '{}'", termo);

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
        log.debug(
            "[GrupoTematicoService] pesquisarGrupoTematico - '{}' não corresponde a nenhuma categoria",
            busca);
      }

      resultados.addAll(grupoTematicoRepository.findByTituloContainingIgnoreCase(busca));
      resultados.addAll(grupoTematicoRepository.buscarPorNomeBairro(busca));

      if (resultados.isEmpty()) {
        log.info(
            "[GrupoTematicoService] pesquisarGrupoTematico - Nenhum resultado para '{}'", busca);
        throw new RuntimeException("Nenhum grupo encontrado");
      }

      log.info(
          "[GrupoTematicoService] pesquisarGrupoTematico - {} resultado(s) encontrado(s) para '{}'",
          resultados.size(),
          busca);
      return resultados.stream().map(ListarGrupoTematicoDTO::new).toList();

    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      log.error(
          "[GrupoTematicoService] pesquisarGrupoTematico - Erro inesperado ao pesquisar '{}'",
          busca,
          e);
      throw new RuntimeException("Erro inesperado ao realizar a pesquisa");
    }
  }

  // Verificar se o usuário autenticado é participante de um grupo
  public MembroStatusResponseDTO verificarParticipacao(Long grupoId, Perfil perfilLogado) {
    log.info(
        "[GrupoTematicoService] verificarParticipacao - Verificando participação do usuário {} no grupo {}",
        perfilLogado.getUsuario().getNome(),
        grupoId);
    MembroStatusResponseDTO status =
        participanteGrupoRepository
            .findByGrupoIdAndUsuarioId(grupoId, perfilLogado.getUsuario().getId())
            .map(p -> new MembroStatusResponseDTO(true, p.getRole().name()))
            .orElse(new MembroStatusResponseDTO(false, null));
    log.info(
        "[GrupoTematicoService] verificarParticipacao - Usuário {} no grupo {}: participante={}, role={}",
        perfilLogado.getUsuario().getNome(),
        grupoId,
        status.participante(),
        status.role());
    return status;
  }

  // excluir
  @Transactional
  public void excluirGrupo(Long grupoId, Perfil perfilLogado) {
    log.info(
        "[GrupoTematicoService] excluirGrupo - Usuário {} tentando excluir grupo {}",
        perfilLogado.getUsuario().getNome(),
        grupoId);

    ParticipanteGrupo executor =
        participanteGrupoRepository
            .findByGrupoIdAndUsuarioId(grupoId, perfilLogado.getUsuario().getId())
            .orElseThrow(
                () -> new RuntimeException("Você não tem permissão para acessar este grupo."));

    if (executor.getRole() != GrupoRole.CRIADORA) {
      log.warn(
          "[GrupoTematicoService] excluirGrupo - Usuário {} sem permissão para excluir grupo {}",
          perfilLogado.getUsuario().getNome(),
          grupoId);
      throw new RuntimeException("Ação negada: Apenas a criadora original pode excluir o grupo.");
    }

    GrupoTematico grupo =
        grupoTematicoRepository
            .findById(grupoId)
            .orElseThrow(() -> new RuntimeException("Grupo não encontrado."));

    grupoTematicoRepository.delete(grupo);
    log.info(
        "[GrupoTematicoService] excluirGrupo - Grupo {} excluído com sucesso pelo usuário {}",
        grupoId,
        perfilLogado.getUsuario().getNome());
  }

  // Denuncia grupo
  @Transactional
  public void denunciarGrupo(Long grupoId, Perfil perfilLogado, String descricao) {
    log.info(
        "[GrupoTematicoService] denunciarGrupo - Usuário {} denunciando grupo {}",
        perfilLogado.getUsuario().getNome(),
        grupoId);

    GrupoTematico grupo =
        grupoTematicoRepository
            .findById(grupoId)
            .orElseThrow(() -> new RuntimeException("Grupo não encontrado"));

    Usuario usuario = perfilLogado.getUsuario();

    boolean participa =
        participanteGrupoRepository.existsByGrupoIdAndUsuarioId(grupoId, usuario.getId());
    if (!participa) {
      log.warn(
          "[GrupoTematicoService] denunciarGrupo - Usuário {} não participa do grupo {} e tentou denunciar",
          usuario.getNome(),
          grupoId);
      throw new RuntimeException("Você não pode denunciar um grupo do qual não participa.");
    }

    if (denunciarGrupoRepository.existsByGrupoIdAndUsuarioId(grupoId, usuario.getId())) {
      log.warn(
          "[GrupoTematicoService] denunciarGrupo - Usuário {} já denunciou o grupo {}",
          usuario.getId(),
          grupoId);
      throw new RuntimeException("Você já denunciou este grupo.");
    }

    DenunciarGrupo denuncia =
        DenunciarGrupo.builder()
            .grupo(grupo)
            .usuario(usuario)
            .status(StatusDenuncia.PENDENTE)
            .descricao(descricao)
            .verdadeira(ConsistenciaDenuncia.VERIFICANDO)
            .build();

    denunciarGrupoRepository.save(denuncia);
    log.info(
        "[GrupoTematicoService] denunciarGrupo - Denúncia registrada com sucesso. Grupo: {}, Usuário: {}",
        grupoId,
        usuario.getId());

    long totalPendentes =
        denunciarGrupoRepository.countByGrupoIdAndStatus(grupoId, StatusDenuncia.PENDENTE);
    log.info(
        "[GrupoTematicoService] denunciarGrupo - Total de denúncias PENDENTE no grupo {}: {}",
        grupoId,
        totalPendentes);

    if (totalPendentes % LIMIAR_NOTIFICACAO_DENUNCIAS == 0) {
      log.info(
          "[GrupoTematicoService] denunciarGrupo - Limiar de {} atingido. Notificando admin: {}",
          LIMIAR_NOTIFICACAO_DENUNCIAS,
          adminEmail);
      emailService.notificarDenunciaGrupo(adminEmail, grupo.getTitulo(), totalPendentes);
    }
  }

  @Transactional
  public void banirParticipante(
      Long grupoId, UUID usuarioBanidoId, String motivo, Perfil perfilLogado) {
    log.info(
        "[GrupoTematicoService] banirParticipante - Usuário {} tentando banir {} do grupo {}",
        perfilLogado.getUsuario().getNome(),
        usuarioBanidoId,
        grupoId);

    ParticipanteGrupo executor =
        participanteGrupoRepository
            .findByGrupoIdAndUsuarioId(grupoId, perfilLogado.getUsuario().getId())
            .orElseThrow(() -> new RuntimeException("Você não participa deste grupo."));

    if (executor.getRole() != GrupoRole.CRIADORA) {
      log.warn(
          "[GrupoTematicoService] banirParticipante - Usuário {} sem permissão para banir no grupo {}",
          perfilLogado.getUsuario().getNome(),
          grupoId);
      throw new RuntimeException("Apenas a criadora pode banir participantes.");
    }

    ParticipanteGrupo participante =
        participanteGrupoRepository
            .findByGrupoIdAndUsuarioId(grupoId, usuarioBanidoId)
            .orElseThrow(() -> new RuntimeException("Participante não encontrado."));

    if (participante.getRole() == GrupoRole.CRIADORA) {
      log.warn(
          "[GrupoTematicoService] banirParticipante - Tentativa de banir a criadora do grupo {}",
          grupoId);
      throw new RuntimeException("A criadora não pode ser banida.");
    }

    participante.setAtivo(false);
    participante.setMotivoBanimento(motivo);
    participanteGrupoRepository.save(participante);
    log.info(
        "[GrupoTematicoService] banirParticipante - Usuário {} banido do grupo {} com sucesso. Motivo: {}",
        usuarioBanidoId,
        grupoId,
        motivo);
  }

  public Page<DenunciaGrupoResponseDTO> listarDenuncias(
      DenunciaGrupoFilterDTO filtro, int pagina, int tamanho) {
    log.info(
        "[GrupoTematicoService] listarDenuncias - Listando denúncias. Página: {}, Tamanho: {}",
        pagina,
        tamanho);

    Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by("id").descending());
    Specification<DenunciarGrupo> spec = DenunciarGrupoSpecification.filtrar(filtro);
    Page<DenunciarGrupo> denuncias = denunciarGrupoRepository.findAll(spec, pageable);

    log.info(
        "[GrupoTematicoService] listarDenuncias - {} denúncia(s) encontrada(s) na página {}",
        denuncias.getNumberOfElements(),
        pagina);
    return denuncias.map(DenunciaGrupoResponseDTO::new);
  }

  @Transactional
  public DenunciarGrupo atualizarParcial(Long id, AtualizarDenunciaDTO dto) {
    log.info(
        "[GrupoTematicoService] atualizarParcial - Atualizando denúncia ID: {}", id);

    DenunciarGrupo denuncia =
        denunciarGrupoRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Denúncia não encontrada com o ID: " + id));

    if (dto.status() != null && !dto.status().isBlank()) {
      try {
        StatusDenuncia novoStatus = StatusDenuncia.valueOf(dto.status().trim().toUpperCase());
        denuncia.setStatus(novoStatus);
        log.info(
            "[GrupoTematicoService] atualizarParcial - Status da denúncia {} atualizado para {}",
            id,
            novoStatus);
      } catch (IllegalArgumentException e) {
        log.warn(
            "[GrupoTematicoService] atualizarParcial - Status inválido informado: '{}'",
            dto.status());
      }
    }

    if (dto.descricao() != null && !dto.descricao().isBlank()) {
      denuncia.setDescricao(dto.descricao());
    }

    if (dto.verdadeira() != null && !dto.verdadeira().isBlank()) {
      try {
        ConsistenciaDenuncia consistencia =
            ConsistenciaDenuncia.valueOf(dto.verdadeira().trim().toUpperCase());
        denuncia.setVerdadeira(consistencia);
        log.info(
            "[GrupoTematicoService] atualizarParcial - Consistência da denúncia {} atualizada para {}",
            id,
            consistencia);
      } catch (IllegalArgumentException e) {
        log.warn(
            "[GrupoTematicoService] atualizarParcial - Consistência inválida informada: '{}'",
            dto.verdadeira());
      }
    }

    DenunciarGrupo salva = denunciarGrupoRepository.save(denuncia);
    log.info(
        "[GrupoTematicoService] atualizarParcial - Denúncia {} atualizada com sucesso", id);
    return salva;
  }
}
