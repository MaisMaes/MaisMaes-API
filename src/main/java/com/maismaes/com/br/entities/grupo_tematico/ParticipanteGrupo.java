package com.maismaes.com.br.entities.grupo_tematico;

import com.maismaes.com.br.entities.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(
    name = "participante_grupo",
    uniqueConstraints = {
      @UniqueConstraint(
          columnNames = {"grupo_id", "usuario_id"}) // Garante a não repetição do usuario no grupo
    })
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipanteGrupo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "grupo_id", nullable = false)
  private GrupoTematico grupo;

  @ManyToOne
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @Column(nullable = false)
  private LocalDateTime dataAdesao;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default // precisa disso para não sobrescrever o valor padrão
  private GrupoRole role = GrupoRole.PARTICIPANTE;
}
