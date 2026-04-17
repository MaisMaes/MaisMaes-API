package com.maismaes.com.br.entities.grupo_tematico;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@Table(name = "grupo_tematico")
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class GrupoTematico {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotNull
    private String titulo;

    @Column(nullable = false)
    @NotNull
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "categorias", nullable = false)
    private Categoria categorias;

    @Column
    private String bairro;

    @Column(nullable = false)
    @NotNull
    private boolean privado;

    @Column(nullable = false)
    @NotNull
    @Max(100)
    @Min(1)
    private Integer numeroParticipantes;

    @Column(nullable = false)
    @NotNull
    private boolean video;

    @Column(nullable = false)
    @NotNull
    private boolean audio;

    @Column(nullable = false)
    @NotNull
    private boolean imagem;

    @Column(nullable = false)
    @NotNull
    private boolean documento;

}
