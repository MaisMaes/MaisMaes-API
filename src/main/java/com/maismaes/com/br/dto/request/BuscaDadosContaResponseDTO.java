package com.maismaes.com.br.dto.request;

import com.maismaes.com.br.entities.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record BuscaDadosContaResponseDTO(
        UUID id,
        String nome,
        String email,
        String telefone,
        String role
) {
    public BuscaDadosContaResponseDTO(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getNome(),
                usuario.getPerfil().getPerfilEmail(),
                usuario.getTelefone(),
                usuario.getPerfil().getRole().name()
        );
    }
}
