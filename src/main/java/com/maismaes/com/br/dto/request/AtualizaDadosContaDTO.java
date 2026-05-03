package com.maismaes.com.br.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record AtualizaDadosContaDTO(
    String nome,
    @Pattern(
            regexp = "^\\(\\d{2}\\)\\s9\\d{4}-\\d{4}$",
            message = "Telefone deve estar no formato: (XX) 9XXXX-XXXX")
        String telefone,
    @Pattern(
            regexp = "^((?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,})?$",
            message = "Senha não atende aos critérios de segurança")
        String senha,
    @Email(message = "Email inválido") String email) {}
