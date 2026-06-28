package com.maismaes.com.br.service;

import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.exception.CodigoRecuperacaoInvalidoException;
import com.maismaes.com.br.repository.PerfilRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecuperacaoSenhaService {

  private final PerfilRepository perfilRepository;
  private final EmailService emailService;
  private final BCryptPasswordEncoder passwordEncoder;

  @Value("${recuperacao-senha.expiracao-minutos:10}")
  private long expiracaoMinutos;

  private static final SecureRandom RANDOM = new SecureRandom();
  private static final int MAX_TENTATIVAS_GERACAO = 5;

  private final Map<String, CodigoRecuperacao> codigos = new ConcurrentHashMap<>();

  public boolean solicitarRecuperacao(String email) {
    Perfil perfil = perfilRepository.findByPerfilEmail(email);
    if (perfil == null) {
      throw new EntityNotFoundException("Perfil não encontrado para o e-mail: " + email);
    }

    codigos.values().removeIf(reg -> reg.email().equalsIgnoreCase(email));

    String codigo = gerarCodigoUnico();
    Instant expiraEm = Instant.now().plus(expiracaoMinutos, ChronoUnit.MINUTES);
    codigos.put(codigo, new CodigoRecuperacao(email, expiraEm));

    return emailService.enviarCodigoRecuperacao(email, codigo);
  }

  public void redefinirSenha(String codigo, String novaSenha) {
    CodigoRecuperacao registro = codigos.get(codigo);

    if (registro == null) {
      throw new CodigoRecuperacaoInvalidoException("Código inválido ou expirado");
    }

    if (registro.expiraEm().isBefore(Instant.now())) {
      codigos.remove(codigo);
      throw new CodigoRecuperacaoInvalidoException("Código inválido ou expirado");
    }

    Perfil perfil = perfilRepository.findByPerfilEmail(registro.email());
    if (perfil == null) {
      codigos.remove(codigo);
      throw new CodigoRecuperacaoInvalidoException("Código inválido ou expirado");
    }

    perfil.setSenha(passwordEncoder.encode(novaSenha));
    perfilRepository.save(perfil);
    codigos.remove(codigo);
  }

  private String gerarCodigoUnico() {
    for (int i = 0; i < MAX_TENTATIVAS_GERACAO; i++) {
      String codigo = String.format("%06d", RANDOM.nextInt(1_000_000));
      if (!codigos.containsKey(codigo)) {
        return codigo;
      }
    }
    return String.format("%06d", RANDOM.nextInt(1_000_000));
  }

  private record CodigoRecuperacao(String email, Instant expiraEm) {}
}
