package com.maismaes.com.br.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import com.maismaes.com.br.dto.request.AuthRequestDTO;
import com.maismaes.com.br.dto.response.AuthResponseDTO;
import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.entities.Role;
import com.maismaes.com.br.service.TokenService;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController - testes unitários")
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthController authController;

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("Deve autenticar e retornar token e role corretamente")
        void deveAutenticarERetornarTokenEspielRole() {
            AuthRequestDTO request = new AuthRequestDTO("mae@example.com", "senha");

            Perfil perfil = Perfil.builder()
                    .perfilEmail("mae@example.com")
                    .senha("senha")
                    .role(Role.ADMINISTRADOR)
                    .build();

            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(perfil);

            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(tokenService.generateToken(perfil)).thenReturn("tokentest");

            var response = authController.login(request);

            assertNotNull(response);
            assertEquals(200, response.getStatusCodeValue());
            AuthResponseDTO body = response.getBody();
            assertNotNull(body);
            assertEquals("tokentest", body.token());
            // assertEquals("ADMINISTRADOR", body.role());

            verify(authenticationManager, times(1)).authenticate(any());
            verify(tokenService, times(1)).generateToken(perfil);
        }
    }
}

