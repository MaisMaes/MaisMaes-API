package com.maismaes.com.br.entities.grupo_tematico;

import com.maismaes.com.br.entities.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Table(
    name = "denunciar_grupo",
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
public class DenunciarGrupo {

  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @ManyToOne
  @JoinColumn(name = "grupo_id", nullable = false)
  private GrupoTematico grupo;

  @Enumerated(EnumType.STRING)
  @Column
  private StatusDenuncia status;

  @Column private String descricao;

  @CreationTimestamp
  @Column(name = "data_denunciada", nullable = false, updatable = false)
  private LocalDateTime dataDenunciada;

  @UpdateTimestamp
  @Column(name = "atualizado_em", nullable = false)
  private LocalDateTime atualizadoEm;
}
