package com.maismaes.com.br.dto.response;

import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.entities.PerfilStatus;
import com.maismaes.com.br.entities.Role;

import java.util.UUID;

public record DadosPerfilResponseDTo(UUID id, PerfilStatus status, String perfilEmail, Role role ) {
    public DadosPerfilResponseDTo(Perfil perfil){
        this(
             perfil.getId(),
             perfil.getStatus(),
             perfil.getPerfilEmail(),
             perfil.getRole()
        );
    }
}
