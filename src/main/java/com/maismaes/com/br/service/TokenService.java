package com.maismaes.com.br.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.maismaes.com.br.entities.Perfil;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

  @Value("${security.secret}")
  private String secret;

  private Instant getExpirationDate() {
    return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.ofHours(-3));
  }

  public String generateToken(Perfil perfil) {
    try {
      Algorithm algorithm = Algorithm.HMAC256(secret);

      return JWT.create()
          .withIssuer("maismaes_api")
          .withSubject(perfil.getUsername()) // email foi definido como username
          .withClaim("role", perfil.getRole().toString())
          .withClaim("id", perfil.getId().toString())
          .withExpiresAt(getExpirationDate())
          .sign(algorithm);

    } catch (JWTCreationException e) {
      throw new RuntimeException("Erro ao gerar o token", e);
    }
  }

  public String validateToken(String token) {
    try {
      Algorithm algorithm = Algorithm.HMAC256(secret);

      return JWT.require(algorithm).withIssuer("maismaes_api").build().verify(token).getSubject();

    } catch (JWTVerificationException e) {
      return ""; // Retorna uma string vazia em caso de erro na validação do token
    }
  }
}
