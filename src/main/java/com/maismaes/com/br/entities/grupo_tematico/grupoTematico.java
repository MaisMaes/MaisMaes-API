package com.maismaes.com.br.entities.grupo_tematico;

import java.util.HashSet;
import java.util.Set;

import com.maismaes.com.br.entities.Usuario;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
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

    @ManyToOne
    @JoinColumn(name = "criador_id", nullable = false)
    private Usuario criador;

    @Column(nullable = false)
    @NotNull
    private String titulo;

    @Column(nullable = false)
    @NotNull
    private String descricao;

    // @Column(name = "foto_url")
    // private String fotoUrl;

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
    private Integer numeroParticipantes;

    @Column(name = "tempo_entre_mensagens")
    private Integer tempoEntreMensagens;

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

    @OneToMany(mappedBy = "grupo", cascade = {CascadeType.ALL, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default // Garante que o Lombok não ignore a inicialização
    private Set<ParticipanteGrupo> participantes = new HashSet<>();

}
