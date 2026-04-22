package com.maismaes.com.br.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record EditarGrupoTematicoRequestDTO(
    @NotBlank 
    String titulo,

    @NotBlank 
    String descricao,

    @NotBlank 
    String categorias,

    @NotBlank 
    String bairro,

    boolean privado,
    
    @Max(100) 
    Integer numeroParticipantes,

    @Min(0) 
    Integer tempoEntreMensagens,

    boolean video,
    boolean audio,
    boolean imagem,
    boolean documento

) {}

