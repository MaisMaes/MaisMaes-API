package com.maismaes.com.br.controller;

import com.maismaes.com.br.dto.request.BuscaDadosContaResponseDTO;
import com.maismaes.com.br.dto.request.CadastroUsuarioRequestDTO;
import com.maismaes.com.br.dto.response.CadastroUsuarioResponseDTO;
import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.entities.Role;
import com.maismaes.com.br.entities.Usuario;
import com.maismaes.com.br.exception.UsuarioNaoEncontradoException;
import com.maismaes.com.br.service.TokenService;
import com.maismaes.com.br.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioController - testes unitários")
class UsuarioControllerTest {

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private UsuarioController usuarioController;

    @Nested
    @DisplayName("cadastrarUsuario")
    class CadastrarUsuario {

        @Test
        @DisplayName("Deve criar usuário, gerar token e retornar 201 com token")
        void deveCriarUsuarioGerarTokenERetornar201ComToken() {
            var dto = new CadastroUsuarioRequestDTO(
                    "Maria",
                    "maria@example.com",
                    "81 912345678",
                    "senha"
            );

            when(bCryptPasswordEncoder.encode("senha")).thenReturn("encodedSenha");

            Perfil perfil = Perfil.builder()
                    .perfilEmail("maria@example.com")
                    .senha("encodedSenha")
                    .role(Role.MAE_SOLO)
                    .build();

            Usuario salvo = Usuario.builder()
                    .id(UUID.fromString("00000000-0000-0000-0000-000000000002"))
                    .nome("Maria")
                    .email("maria@example.com")
                    .telefone("81 912345678")
                    .perfil(perfil)
                    .build();

            when(usuarioService.cadastrarUsuario(any(Usuario.class))).thenReturn(salvo);
            when(tokenService.generateToken(perfil)).thenReturn("token123");

            ResponseEntity<CadastroUsuarioResponseDTO> response = usuarioController.cadastrarUsuario(dto);

            assertEquals(201, response.getStatusCodeValue());
            assertNotNull(response.getBody());
            assertEquals("token123", response.getBody().token());

            verify(bCryptPasswordEncoder, times(1)).encode("senha");
            verify(usuarioService, times(1)).cadastrarUsuario(any(Usuario.class));
            verify(tokenService, times(1)).generateToken(perfil);
        }

        @Test
        @DisplayName("Deve propagar exceção quando o serviço falhar ao cadastrar")
        void devePropagarExcecaoQuandoServicoFalhar() {
            var dto = new CadastroUsuarioRequestDTO(
                    "Joao",
                    "joao@example.com",
                    "81 912345679",
                    "senha"
            );

            when(bCryptPasswordEncoder.encode("senha")).thenReturn("encoded");
            when(usuarioService.cadastrarUsuario(any(Usuario.class))).thenThrow(new RuntimeException("erro salvar"));

            RuntimeException ex = assertThrows(RuntimeException.class, () -> usuarioController.cadastrarUsuario(dto));

            assertEquals("erro salvar", ex.getMessage());
            verify(bCryptPasswordEncoder, times(1)).encode("senha");
            verify(usuarioService, times(1)).cadastrarUsuario(any(Usuario.class));
            verify(tokenService, times(0)).generateToken(any());
        }

    }


    @Nested
    @DisplayName("buscarMinhaConta")
    class BuscarMinhaConta {

        @Test
        @DisplayName("Deve retornar dados do usuário logado")
        void deveRetornarDadosDoUsuarioLogado() {

            UUID uid = UUID.randomUUID();
            Perfil perfil = Perfil.builder()
                    .perfilEmail("mae@example.com")
                    .role(Role.MAE_SOLO)
                    .build();

            Usuario usuario = Usuario.builder()
                    .id(uid)
                    .nome("Maria")
                    .email("mae@example.com")
                    .telefone("81999999999")
                    .perfil(perfil)
                    .build();

            BuscaDadosContaResponseDTO dto = new BuscaDadosContaResponseDTO(usuario);

            when(usuarioService.buscarDadosConta("mae@example.com"))
                    .thenReturn(dto);


            ResponseEntity<BuscaDadosContaResponseDTO> response =
                    usuarioController.buscarMinhaConta(perfil);


            assertEquals(200, response.getStatusCodeValue());
            assertNotNull(response.getBody());

            var body = response.getBody();

            assertEquals("Maria", body.nome());
            assertEquals("mae@example.com", body.email());
            assertEquals("81999999999", body.telefone());
            assertEquals("MAE_SOLO", body.role());

            verify(usuarioService, times(1))
                    .buscarDadosConta("mae@example.com");
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não for encontrado")
        void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {

            Perfil perfil = Perfil.builder()
                    .perfilEmail("naoexiste@example.com")
                    .build();

            when(usuarioService.buscarDadosConta("naoexiste@example.com"))
                    .thenThrow(new UsuarioNaoEncontradoException());

            assertThrows(UsuarioNaoEncontradoException.class,
                    () -> usuarioController.buscarMinhaConta(perfil));

            verify(usuarioService, times(1))
                    .buscarDadosConta("naoexiste@example.com");
        }
    }






}

