package com.maismaes.com.br.entities.grupo_tematico;

import java.time.LocalDateTime;

import com.maismaes.com.br.entities.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
    name = "favorito_grupo",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {
                "usuario_id",
                "grupo_id"
            }
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoritoGrupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "usuario_id",
        nullable = false
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "grupo_id",
        nullable = false
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private GrupoTematico grupo;

    @Column(nullable = false)
    private LocalDateTime dataFavorito;

    @PrePersist
    public void prePersist() {
        if (dataFavorito == null) {
            dataFavorito = LocalDateTime.now();
        }
    }
}
