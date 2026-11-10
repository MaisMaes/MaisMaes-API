package com.maismaes.com.br.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.maismaes.com.br.repository.DelegaciaRepository;
import com.maismaes.com.br.service.DelegaciaImportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DelegaciaInicializador {

  private static final int TENTATIVAS = 3;

  private final DelegaciaRepository delegaciaRepository;
  private final DelegaciaImportService delegaciaImportService;

  @Value("${delegacias.estado-inicial:BR-PE}")
  private String estadoInicial;

  @Async
  @EventListener(ApplicationReadyEvent.class)
  public void importarSeVazio() {
    if (delegaciaRepository.count() > 0) {
      log.info("[Delegacias] Tabela já populada, importação inicial ignorada");
      return;
    }

    for (int tentativa = 1; tentativa <= TENTATIVAS; tentativa++) {
      try {
        int total = delegaciaImportService.importar(estadoInicial);
        if (total > 0) {
          log.info("[Delegacias] Importação inicial concluída | {} delegacias", total);
          return;
        }
        log.warn(
            "[Delegacias] Importação inicial retornou 0 (tentativa {}/{})", tentativa, TENTATIVAS);
      } catch (Exception e) {
        log.warn(
            "[Delegacias] Falha na importação inicial (tentativa {}/{}): {}",
            tentativa,
            TENTATIVAS,
            e.getMessage());
      }

      if (tentativa < TENTATIVAS) {
        try {
          Thread.sleep(tentativa * 30_000L); // espera 30s, depois 60s
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
      }
    }

    log.error(
        "[Delegacias] Não foi possível importar na inicialização. Será tentado de novo no próximo start.");
  }
}