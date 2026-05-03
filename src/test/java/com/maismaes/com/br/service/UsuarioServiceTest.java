package com.maismaes.com.br.service;

import com.maismaes.com.br.dto.request.BuscaDadosContaResponseDTO;
import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.entities.Role;
import com.maismaes.com.br.entities.Usuario;
import com.maismaes.com.br.exception.UsuarioNaoEncontradoException;
import com.maismaes.com.br.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - testes unitários")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private com.maismaes.com.br.utils.UserValidationUtils userValidationUtils;

    @Mock
    private com.maismaes.com.br.repository.PerfilRepository perfilRepository;

    @Mock
    private org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Nested
    @DisplayName("cadastrarUsuario")
    class CadastrarUsuario {

        @Test
        @DisplayName("Deve salvar um usuário válido e retornar o usuário salvo")
        void deveSalvarUsuarioValidoEVoltarUsuarioSalvo() {
            Perfil perfil = criarPerfil("mae@example.com", "senha");

            Usuario entrada = criarUsuario(null, "Maria", "maria@example.com", "999999999", perfil);

            Usuario salvo = criarUsuario(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Maria", "maria@example.com", "999999999", perfil);

            when(usuarioRepository.save(entrada)).thenReturn(salvo);

            Usuario resultado = usuarioService.cadastrarUsuario(entrada);

            assertSame(salvo, resultado);
            verify(usuarioRepository, times(1)).save(entrada);
        }

        @Test
        @DisplayName("Deve propagar exceção quando o repositório falhar")
        void devePropagarExcecaoQuandoRepositorioFalhar() {
            Perfil perfil = criarPerfil("erro@example.com", "senha");

            Usuario entrada = criarUsuario(null, "Erro", "erro@example.com", "000000000", perfil);

            when(usuarioRepository.save(entrada)).thenThrow(new RuntimeException("falha na base"));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> usuarioService.cadastrarUsuario(entrada));

            assertEquals("falha na base", ex.getMessage());
            verify(usuarioRepository, times(1)).save(entrada);
        }

        @Test
        @DisplayName("Deve lançar NullPointerException quando usuário for nulo")
        void deveLancarExcecaoQuandoUsuarioForNulo() {
            assertThrows(NullPointerException.class,
                    () -> usuarioService.cadastrarUsuario(null));

            verify(usuarioRepository, never()).save(any());
        }
    }


    @Nested
    @DisplayName("buscarDadosConta")
    class BuscarDadosConta {

        @Test
        @DisplayName("Deve retornar dados do usuário quando encontrado")
        void deveRetornarDadosDoUsuario() {


            Perfil perfil = Perfil.builder()
                    .perfilEmail("mae@example.com")
                    .role(Role.MAE_SOLO)
                    .build();

            Usuario usuario = Usuario.builder()
                    .nome("Maria")
                    .telefone("81999999999")
                    .perfil(perfil)
                    .build();

            when(usuarioRepository.findByPerfil_PerfilEmail("mae@example.com"))
                    .thenReturn(Optional.of(usuario));


            BuscaDadosContaResponseDTO response =
                    usuarioService.buscarDadosConta("mae@example.com");


            assertNotNull(response);
            assertEquals("Maria", response.nome());
            assertEquals("mae@example.com", response.email());
            assertEquals("81999999999", response.telefone());
            assertEquals("MAE_SOLO", response.role());

            verify(usuarioRepository, times(1))
                    .findByPerfil_PerfilEmail("mae@example.com");
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não encontrado")
        void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {


            when(usuarioRepository.findByPerfil_PerfilEmail("naoexiste@example.com"))
                    .thenReturn(Optional.empty());


            assertThrows(UsuarioNaoEncontradoException.class,
                    () -> usuarioService.buscarDadosConta("naoexiste@example.com"));

            verify(usuarioRepository, times(1))
                    .findByPerfil_PerfilEmail("naoexiste@example.com");
        }
    }

    // helpers para criar massa de teste
    private Perfil criarPerfil(String email, String senha) {
        return Perfil.builder()
                .perfilEmail(email)
                .senha(senha)
                .build();
    }

    private Usuario criarUsuario(UUID id, String nome, String email, String telefone, Perfil perfil) {
        Usuario.UsuarioBuilder builder = Usuario.builder()
                .nome(nome)
                .email(email)
                .telefone(telefone)
                .perfil(perfil);

        if (id != null) {
            builder.id(id);
        }

        return builder.build();
    }
}



