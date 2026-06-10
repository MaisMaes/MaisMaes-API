package com.maismaes.com.br.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.maismaes.com.br.dto.response.DetalheGrupoResponseDTO;
import com.maismaes.com.br.dto.response.ListarGrupoTematicoDTO;
import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.entities.Usuario;
import com.maismaes.com.br.entities.grupo_tematico.Categoria;
import com.maismaes.com.br.entities.grupo_tematico.FavoritoGrupo;
import com.maismaes.com.br.entities.grupo_tematico.GrupoRole;
import com.maismaes.com.br.entities.grupo_tematico.GrupoTematico;
import com.maismaes.com.br.entities.grupo_tematico.ParticipanteGrupo;
import com.maismaes.com.br.repository.FavoritoGrupoRepository;
import com.maismaes.com.br.repository.GrupoTematicoRepository;
import com.maismaes.com.br.repository.ParticipanteGrupoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GrupoTematicoServiceTest {

  @Mock private GrupoTematicoRepository grupoTematicoRepository;

  @Mock private ParticipanteGrupoRepository participanteGrupoRepository;

  @Mock private FavoritoGrupoRepository favoritoGrupoRepository;

  @InjectMocks private GrupoTematicoService grupoTematicoService;

  @Test
  void deveFavoritarGrupoQuandoAindaNaoFavoritado() {
    Long grupoId = 1L;
    Perfil perfil = criarPerfilComUsuario(UUID.randomUUID());
    GrupoTematico grupo = criarGrupo(grupoId);

    when(favoritoGrupoRepository.existsByGrupoIdAndUsuarioId(
            grupoId, perfil.getUsuario().getId()))
        .thenReturn(false);
    when(grupoTematicoRepository.findById(grupoId)).thenReturn(Optional.of(grupo));

    grupoTematicoService.favoritarGrupo(grupoId, perfil);

    ArgumentCaptor<FavoritoGrupo> favoritoCaptor = ArgumentCaptor.forClass(FavoritoGrupo.class);
    verify(favoritoGrupoRepository).save(favoritoCaptor.capture());

    FavoritoGrupo favorito = favoritoCaptor.getValue();
    assertSame(grupo, favorito.getGrupo());
    assertSame(perfil.getUsuario(), favorito.getUsuario());
    assertNotNull(favorito.getDataFavorito());
  }

  @Test
  void naoDeveDuplicarFavoritoDoMesmoGrupoParaMesmoUsuario() {
    Long grupoId = 1L;
    Perfil perfil = criarPerfilComUsuario(UUID.randomUUID());

    when(favoritoGrupoRepository.existsByGrupoIdAndUsuarioId(
            grupoId, perfil.getUsuario().getId()))
        .thenReturn(true);

    grupoTematicoService.favoritarGrupo(grupoId, perfil);

    verify(grupoTematicoRepository, never()).findById(anyLong());
    verify(favoritoGrupoRepository, never()).save(any());
  }

  @Test
  void deveRemoverGrupoDosFavoritos() {
    Long grupoId = 1L;
    Perfil perfil = criarPerfilComUsuario(UUID.randomUUID());

    when(grupoTematicoRepository.existsById(grupoId)).thenReturn(true);

    grupoTematicoService.removerFavoritoGrupo(grupoId, perfil);

    verify(favoritoGrupoRepository).deleteByGrupoIdAndUsuarioId(
        grupoId, perfil.getUsuario().getId());
  }

  @Test
  void deveListarGruposFavoritosDoUsuario() {
    Perfil perfil = criarPerfilComUsuario(UUID.randomUUID());
    GrupoTematico grupo = criarGrupo(1L);
    FavoritoGrupo favorito =
        FavoritoGrupo.builder().grupo(grupo).usuario(perfil.getUsuario()).build();

    when(favoritoGrupoRepository.findByUsuarioId(perfil.getUsuario().getId()))
        .thenReturn(List.of(favorito));

    List<ListarGrupoTematicoDTO> resultado = grupoTematicoService.listarGruposFavoritos(perfil);

    assertEquals(1, resultado.size());
    assertEquals(grupo.getId(), resultado.get(0).id());
  }

  @Test
  void deveInformarSeUsuarioLogadoFavoritouNoDetalheDoGrupo() {
    Long grupoId = 1L;
    UUID usuarioId = UUID.randomUUID();
    Usuario usuario = Usuario.builder().id(usuarioId).nome("Maria").build();
    GrupoTematico grupo = criarGrupo(grupoId);
    ParticipanteGrupo participante =
        ParticipanteGrupo.builder()
            .grupo(grupo)
            .usuario(usuario)
            .role(GrupoRole.PARTICIPANTE)
            .dataAdesao(LocalDateTime.now())
            .build();
    grupo.getParticipantes().add(participante);

    when(grupoTematicoRepository.findById(grupoId)).thenReturn(Optional.of(grupo));
    when(favoritoGrupoRepository.existsByGrupoIdAndUsuarioId(grupoId, usuarioId)).thenReturn(true);

    DetalheGrupoResponseDTO resultado = grupoTematicoService.obterDetalhes(grupoId, usuarioId);

    assertTrue(resultado.usuarioLogadoFavoritou());
  }

  private Perfil criarPerfilComUsuario(UUID usuarioId) {
    Usuario usuario = Usuario.builder().id(usuarioId).nome("Maria").build();
    Perfil perfil = Perfil.builder().build();

    usuario.setPerfil(perfil);
    perfil.setUsuario(usuario);

    return perfil;
  }

  private GrupoTematico criarGrupo(Long grupoId) {
    return GrupoTematico.builder()
        .id(grupoId)
        .titulo("Grupo de apoio")
        .descricao("Descricao")
        .categorias(Categoria.OUTROS)
        .criador(Usuario.builder().nome("Ana").build())
        .privado(false)
        .numeroParticipantes(20)
        .tempoEntreMensagens(1)
        .build();
  }
}
