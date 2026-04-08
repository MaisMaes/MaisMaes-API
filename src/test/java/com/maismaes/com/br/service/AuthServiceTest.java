package com.maismaes.com.br.service;

import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.repository.PerfilRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - testes unitários")
class AuthServiceTest {

    @Mock
    private PerfilRepository perfilRepository;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("loadUserByUsername")
    class LoadUserByUsername {

        @Test
        @DisplayName("Deve retornar o perfil quando o repositório encontrar o usuário")
        void deveRetornarPerfilQuandoRepositorioEncontrar() {
            Perfil perfil = Perfil.builder()
                    .perfilEmail("mae@example.com")
                    .senha("senha")
                    .build();

            when(perfilRepository.findByPerfilEmail("mae@example.com")).thenReturn(perfil);

            var resultado = authService.loadUserByUsername("mae@example.com");

            assertSame(perfil, resultado);
            verify(perfilRepository, times(1)).findByPerfilEmail("mae@example.com");
        }

        @Test
        @DisplayName("Deve retornar nulo quando o repositório não encontrar o usuário")
        void deveRetornarNuloQuandoRepositorioNaoEncontrar() {
            when(perfilRepository.findByPerfilEmail("nao@existe.com")).thenReturn(null);

            var resultado = authService.loadUserByUsername("nao@existe.com");

            assertNull(resultado);
            verify(perfilRepository, times(1)).findByPerfilEmail("nao@existe.com");
        }
    }
}

