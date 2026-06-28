package com.maismaes.com.br.dto.request;

import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.entities.Role;
import com.maismaes.com.br.entities.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CadastroUsuarioRequestDTO(
    @NotBlank(message = "Nome é obrigatório") String nome,
    @NotBlank(message = "Email é obrigatório") @Email(message = "Email inválido") String email,
    @Pattern(
            regexp = "^\\(\\d{2}\\)\\s9\\d{4}-\\d{4}$",
            message = "Telefone deve estar no formato: (XX) 9XXXX-XXXX")
        String telefone,
    @NotBlank(message = "Senha é obrigatória")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$",
            message = "Senha não atende aos critérios de segurança")
        String senha) {
  public Usuario toUsuarioEntity(String senha) {
//    return Usuario.builder()
//        .nome(nome)
//        .email(email)
//        .telefone(telefone)
//        .perfil(Perfil.builder().perfilEmail(email).senha(senha).role(Role.MAE_SOLO).build())
//        .build();
//  }


      Perfil perfil = Perfil.builder()
              .perfilEmail(email)
              .senha(senha)
              .role(Role.MAE_SOLO)
              .build();

      Usuario usuario = Usuario.builder()
              .nome(nome)
              .email(email)
              .telefone(telefone)
              .perfil(perfil)
              .build();

      perfil.setUsuario(usuario);

      return usuario;
}
}
