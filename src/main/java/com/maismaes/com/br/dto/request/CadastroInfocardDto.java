package com.maismaes.com.br.dto.request;

import com.maismaes.com.br.entities.Infocard;
import com.maismaes.com.br.entities.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;


@Schema(description = "DTO utilizado para cadastro de um novo infocard")
public record CadastroInfocardDto(

        @Schema(
                description = "Título do infocard",
                example = "Auxílio Jurídico Gratuito"
        )

        @NotBlank(message = "O título é obrigatório")
        String titulo,


        @Schema(
                description = "Descrição do infocard",
                example = "Informações sobre assistência jurídica gratuita."
        )
        @NotBlank(message = "A descrição é obrigatória")
        String descricao,


        @Schema(
                description = "URL da imagem do infocard",
                example = "https://meusite.com/imagens/card.jpg"
        )
        @NotBlank(message = "Foto é obrigatória")
        String imagem,

        @Schema(
                description = "Link externo para mais informações",
                example = "https://gov.br/auxilio"
        )
        String link
) {

    public Infocard toEntity(Usuario criador) {
        return Infocard.builder()
                .titulo(titulo)
                .descricao(descricao)
                .imagem(imagem)
                .link(link)
                .criador(criador)
                .ativo(true)
                .destaque(false)
                .build();
    }
}
