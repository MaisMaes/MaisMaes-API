package com.maismaes.com.br.service;


import com.maismaes.com.br.dto.request.AtualizaDto;
import com.maismaes.com.br.dto.request.CadastroInfocardDto;
import com.maismaes.com.br.dto.response.InfoCardResponseDto;
import com.maismaes.com.br.entities.Infocard;
import com.maismaes.com.br.entities.Usuario;
import com.maismaes.com.br.repository.InfocardRepository;
import com.maismaes.com.br.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;


import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InfocardServiceTest {

    @Mock
    private InfocardRepository infocardRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private InfocardService infocardService;

    @Test
    void deveCadastrarInfocard() {
        Usuario usuario = new Usuario();
        CadastroInfocardDto dto = mock(CadastroInfocardDto.class);

        Infocard infocard = new Infocard();
        when(dto.toEntity(usuario)).thenReturn(infocard);

        when(infocardRepository.save(any())).thenReturn(infocard);

        InfoCardResponseDto response = infocardService.cadastrar(dto, usuario);

        assertNotNull(response);
        verify(infocardRepository, times(1)).save(infocard);
    }

    @Test
    void deveBuscarInfocardPorId() {
        UUID id = UUID.randomUUID();

        Infocard infocard = new Infocard();

        when(infocardRepository.findById(id))
                .thenReturn(Optional.of(infocard));

        InfoCardResponseDto response = infocardService.buscarPorId(id);

        assertNotNull(response);
        verify(infocardRepository).findById(id);
    }

    @Test
    void deveLancarExcecaoQuandoInfocardNaoExiste() {
        UUID id = UUID.randomUUID();

        when(infocardRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> infocardService.buscarPorId(id));
    }

    @Test
    void deveListarTodosQuandoAtivoForNulo() {
        Page<Infocard> page = new PageImpl<>(List.of(new Infocard()));

        when(infocardRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<InfoCardResponseDto> result =
                infocardService.listar(0, 10, null);

        assertEquals(1, result.getContent().size());
        verify(infocardRepository).findAll(any(Pageable.class));
    }

    @Test
    void deveListarPorAtivo() {
        Page<Infocard> page = new PageImpl<>(List.of(new Infocard()));

        when(infocardRepository.findByAtivo(eq(true), any(Pageable.class)))
                .thenReturn(page);

        Page<InfoCardResponseDto> result =
                infocardService.listar(0, 10, true);

        assertEquals(1, result.getContent().size());
        verify(infocardRepository).findByAtivo(eq(true), any(Pageable.class));
    }

    @Test
    void deveBuscarPorTitulo() {
        Page<Infocard> page = new PageImpl<>(List.of(new Infocard()));

        when(infocardRepository.findByTituloContainingIgnoreCaseAndAtivoTrue(
                anyString(),
                any(Pageable.class)
        )).thenReturn(page);

        Page<InfoCardResponseDto> result =
                infocardService.buscarPorTitulo("teste", 0, 10);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void deveAtualizarInfocard() {
        UUID id = UUID.randomUUID();

        Infocard infocard = new Infocard();
        infocard.setTitulo("old");

        AtualizaDto dto = mock(AtualizaDto.class);

        when(infocardRepository.findById(id))
                .thenReturn(Optional.of(infocard));

        when(dto.titulo()).thenReturn("novo");

        when(infocardRepository.save(any()))
                .thenReturn(infocard);

        InfoCardResponseDto result =
                infocardService.atualizar(id, dto);

        assertNotNull(result);
        assertEquals("novo", infocard.getTitulo());
    }

    @Test
    void deveDeletarInfocard() {
        UUID id = UUID.randomUUID();

        when(infocardRepository.existsById(id)).thenReturn(true);

        infocardService.deletar(id);

        verify(infocardRepository).deleteById(id);
    }

    @Test
    void deveListarDestaques() {
        Page<Infocard> page = new PageImpl<>(List.of(new Infocard()));

        when(infocardRepository.findByDestaqueTrueAndAtivoTrue(any(Pageable.class)))
                .thenReturn(page);

        Page<InfoCardResponseDto> result =
                infocardService.listarDestaques(0, 10);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void deveAdicionarFavorito() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        Usuario usuario = new Usuario();
        usuario.setFavoritos(new HashSet<>());

        Infocard infocard = new Infocard();

        when(usuarioRepository.findById(userId))
                .thenReturn(Optional.of(usuario));

        when(infocardRepository.findById(cardId))
                .thenReturn(Optional.of(infocard));

        infocardService.adicionarFavorito(userId, cardId);

        assertTrue(usuario.getFavoritos().contains(infocard));
    }

    @Test
    void deveRemoverFavorito() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        Infocard infocard = new Infocard();

        Usuario usuario = new Usuario();
        usuario.setFavoritos(new HashSet<>(Set.of(infocard)));

        when(usuarioRepository.findById(userId))
                .thenReturn(Optional.of(usuario));

        when(infocardRepository.findById(cardId))
                .thenReturn(Optional.of(infocard));

        infocardService.removerFavorito(userId, cardId);

        assertFalse(usuario.getFavoritos().contains(infocard));
    }

    @Test
    void deveListarFavoritos() {
        UUID userId = UUID.randomUUID();

        Infocard infocard = new Infocard();

        Usuario usuario = new Usuario();
        usuario.setFavoritos(new HashSet<>(Set.of(infocard)));

        when(usuarioRepository.findById(userId))
                .thenReturn(Optional.of(usuario));

        Page<InfoCardResponseDto> result =
                infocardService.listarFavoritos(userId, 0, 10);

        assertEquals(1, result.getContent().size());
    }



}
