package com.maismaes.com.br.dto.request;

import com.maismaes.com.br.entities.grupo_tematico.Categoria;
import com.maismaes.com.br.entities.grupo_tematico.GrupoTematico;

import jakarta.validation.constraints.NotNull;

public record CriarGrupoTematicoRequestDTO(
        @NotNull(message = "O título é obrigatório") String titulo,

        @NotNull(message = "A descrição é obrigatória") String descricao,

        @NotNull(message = "A categoria é obrigatória") String categorias,

        @NotNull(message = "O bairro é obrigatório") String bairro,

        @NotNull(message = "A privacidade é obrigatória") boolean privado,

        @NotNull(message = "O número de participantes é obrigatório") Integer numeroParticipantes,

        @NotNull(message = "O tempo entre mensagens é obrigatório") Integer tempoEntreMensagens,

        boolean video,
        boolean audio,
        boolean imagem,
        boolean documento

) {

    public GrupoTematico ToGrupoTematicoEntity() {
        return GrupoTematico.builder()
                .titulo(titulo)
                .descricao(descricao)
                .categorias(Categoria.valueOf(categorias.toUpperCase()))
                .bairro(bairro)
                .privado(privado)
                .numeroParticipantes(numeroParticipantes)
                .video(video)
                .audio(audio)
                .imagem(imagem)
                .documento(documento)
                .build();
    }

}
