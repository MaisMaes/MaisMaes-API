package com.maismaes.com.br.utils;

import com.maismaes.com.br.exception.SenhaIgualException;
import com.maismaes.com.br.exception.VerificarUnicidadeException;
import com.maismaes.com.br.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserValidationUtils {

    private final UsuarioRepository usuarioRepository;

    public void verificarUnicidade(String email, String telefone, UUID usuarioId) {

        if (email != null && usuarioRepository.existsByEmailAndIdNot(email, usuarioId)) {
            throw new VerificarUnicidadeException("Email já cadastrado no sistema!");
        }

        if (telefone != null && usuarioRepository.existsByTelefoneAndIdNot(telefone, usuarioId)) {
            throw new VerificarUnicidadeException("Telefone já cadastrado no sistema!");
        }
    }




}
