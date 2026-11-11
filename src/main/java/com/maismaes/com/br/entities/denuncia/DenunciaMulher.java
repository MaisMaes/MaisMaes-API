package com.maismaes.com.br.entities.denuncia;

import com.maismaes.com.br.entities.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Table(name = "denuncias_mulheres")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DenunciaMulher {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @Column(nullable = false)
  private String nomeUsuario;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String endereco;

  @Column(nullable = false)
  private String contato;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StatusDenunciaMulher status = StatusDenunciaMulher.ABERTA;

  @Column(columnDefinition = "TEXT")
  private String descricao;

  @CreationTimestamp
  @Column(name = "data_denuncia", nullable = false, updatable = false)
  private LocalDateTime dataDenuncia;

  @UpdateTimestamp
  @Column(name = "atualizado_em", nullable = false)
  private LocalDateTime atualizadoEm;
}
