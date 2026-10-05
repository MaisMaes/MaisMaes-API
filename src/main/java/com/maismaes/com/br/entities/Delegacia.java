package com.maismaes.com.br.entities;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(
    name = "delegacias",
    indexes = @Index(name = "idx_delegacia_lat_lon", columnList = "latitude, longitude"))
public class Delegacia {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Identificador na fonte de origem (ex.: "node/123456" do OpenStreetMap).
  // Serve para atualizar em vez de duplicar quando a importação rodar de novo.
  @Column(unique = true)
  private String externalId;

  @Column(nullable = false)
  private String nome;

  private String endereco;

  private String telefone;

  @Column(nullable = false)
  private double latitude;

  @Column(nullable = false)
  private double longitude;
}