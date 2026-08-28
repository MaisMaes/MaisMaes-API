package com.maismaes.com.br.dto.response;

import com.maismaes.com.br.entities.PerfilStatus;
import com.maismaes.com.br.entities.Role;

import java.util.UUID;

public record DadosUsuariosDto(UUID id,
                               String nome,
                               String email,
                               String telefone,
                               PerfilStatus status,
                               Role role) {
}
