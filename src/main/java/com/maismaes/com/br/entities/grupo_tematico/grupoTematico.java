package com.maismaes.com.br.entities.grupo_tematico;

import java.util.UUID;

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
public class grupoTematico {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @NotNull
    private String titulo;

    @Column(nullable = false)
    @NotNull
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name= "categoria", nullable = false)
    private categorias categoria;

    @Column
    private String bairro;

    @Column(nullable = false)
    @NotNull
    private boolean privado;

    @Column(nullable = false)
    @NotNull
    @Max(100)
    @Min(2)
    private Integer numeroParticipantes;

    // @ElementCollection(targetClass = tiposDados.class)
    // @Enumerated(EnumType.STRING)
    // @Column(name = "tipo_dado", nullable = false)
    // private tiposDados tipoDado;


}
