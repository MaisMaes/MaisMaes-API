package com.maismaes.com.br.dto.request;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EditarGrupoTematicoRequestDTO(
    @NotBlank 
    String titulo,

    @NotBlank 
    String descricao,

    @NotBlank 
    String categorias,

    @NotNull
    List<String> bairros,

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

