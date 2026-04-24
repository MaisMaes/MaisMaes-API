package com.maismaes.com.br.service;

import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.exception.CodigoRecuperacaoInvalidoException;
import com.maismaes.com.br.repository.PerfilRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RecuperacaoSenhaService {

    private final PerfilRepository perfilRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${recuperacao-senha.expiracao-minutos:10}")
    private long expiracaoMinutos;

    private static final SecureRandom RANDOM = new SecureRandom();
    private final Map<String, CodigoRecuperacao> codigosPorEmail = new ConcurrentHashMap<>();

    public void solicitarRecuperacao(String email) {
        Perfil perfil = perfilRepository.findByPerfilEmail(email);
        if (perfil == null) {
            return;
        }

        String codigo = gerarCodigo();
        Instant expiraEm = Instant.now().plus(expiracaoMinutos, ChronoUnit.MINUTES);
        codigosPorEmail.put(email, new CodigoRecuperacao(codigo, expiraEm));

        emailService.enviarCodigoRecuperacao(email, codigo);
    }

    public void redefinirSenha(String email, String codigo, String novaSenha) {
        CodigoRecuperacao registro = codigosPorEmail.get(email);

        if (registro == null) {
            throw new CodigoRecuperacaoInvalidoException("Código inválido ou expirado");
        }

        if (registro.expiraEm().isBefore(Instant.now())) {
            codigosPorEmail.remove(email);
            throw new CodigoRecuperacaoInvalidoException("Código inválido ou expirado");
        }

        if (!registro.codigo().equals(codigo)) {
            throw new CodigoRecuperacaoInvalidoException("Código inválido ou expirado");
        }

        Perfil perfil = perfilRepository.findByPerfilEmail(email);
        if (perfil == null) {
            throw new CodigoRecuperacaoInvalidoException("Código inválido ou expirado");
        }

        perfil.setSenha(passwordEncoder.encode(novaSenha));
        perfilRepository.save(perfil);
        codigosPorEmail.remove(email);
    }

    private String gerarCodigo() {
        int valor = RANDOM.nextInt(1_000_000);
        return String.format("%06d", valor);
    }

    private record CodigoRecuperacao(String codigo, Instant expiraEm) {
    }
}

