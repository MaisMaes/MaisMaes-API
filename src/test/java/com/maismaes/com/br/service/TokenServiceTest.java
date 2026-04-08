package com.maismaes.com.br.service;

import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.entities.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TokenService - testes unitários")
class TokenServiceTest {

    private final TokenService tokenService = new TokenService();

    @Nested
    @DisplayName("generateToken / validateToken")
    class GenerateAndValidate {

        @Test
        @DisplayName("Deve gerar um token válido e validar retornando o subject (email)")
        void deveGerarTokenValidoEValidarRetornandoSubject() {
            ReflectionTestUtils.setField(tokenService, "secret", "segredo-teste-123");

            Perfil perfil = Perfil.builder()
                    .id(UUID.fromString("00000000-0000-0000-0000-000000000010"))
                    .perfilEmail("mae@example.com")
                    .senha("senha")
                    .role(Role.MAE_SOLO)
                    .build();

            String token = tokenService.generateToken(perfil);

            assertNotNull(token);
            assertFalse(token.isBlank());

            String subject = tokenService.validateToken(token);

            assertEquals(perfil.getUsername(), subject);
        }

        @Test
        @DisplayName("Deve retornar string vazia ao validar um token inválido")
        void deveRetornarVazioAoValidarTokenInvalido() {
            ReflectionTestUtils.setField(tokenService, "secret", "segredo-teste-123");

            String subject = tokenService.validateToken("token.invalido.qualquer");

            assertEquals("", subject);
        }
    }
}

