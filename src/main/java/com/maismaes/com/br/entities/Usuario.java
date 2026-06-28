package com.maismaes.com.br.entities;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@Table(name = "usuarios")
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String nome;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(unique = true, nullable = false)
  private String telefone;

  @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.PERSIST)
  @JoinColumn(name = "perfil_id", nullable = false, unique = true)
  private Perfil perfil;

  @ManyToMany
  @JoinTable(
      name = "usuario_infocard_favorito",
      joinColumns = @JoinColumn(name = "usuario_id"),
      inverseJoinColumns = @JoinColumn(name = "infocard_id"))
  private Set<Infocard> favoritos = new HashSet<>();
}
