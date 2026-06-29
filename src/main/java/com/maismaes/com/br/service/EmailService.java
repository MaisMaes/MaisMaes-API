package com.maismaes.com.br.service;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  @Value("${mail.api.url:http://localhost:8088/api/mensagens}")
  private String URL_API_MENSAGENS;

  private RestClient restClient;

  @PostConstruct
  public void init() {
    this.restClient = RestClient.create(URL_API_MENSAGENS);
  }

  @Value("${mail.from:nao-responda@maismaes.com.br}")
  private String from;

  public boolean enviarCodigoRecuperacao(String destinatario, String codigo) {
    log.info(
        "[REQUISIÇÃO][EmailService] - Solicitando envio de código de recuperação de senha para API mensagens");
    try {
      restClient
          .post()
          .uri("/recuperar-senha")
          .contentType(MediaType.APPLICATION_JSON)
          .body(Map.of("email", destinatario, "codigo", codigo))
          .retrieve()
          .toBodilessEntity();
      log.info(
          "[REQUISIÇÃO][EmailService] - Código de recuperação de senha enviado com sucesso para: {}",
          destinatario);
      return true;
    } catch (Exception ex) {
      log.error(
          "[REQUISIÇÃO][EmailService] - Falha ao enviar código de recuperação de senha para: {}",
          destinatario,
          ex);
      return false;
    }
  }

  public boolean notificarNovoParticipante(
      String email, String nomeGrupo, String nomeParticipante) {
    log.info(
        "[REQUISIÇÃO][EmailService] - Solicitando notificação de novo participante no grupo '{}' para: {}",
        nomeGrupo,
        email);
    try {
      restClient
          .post()
          .uri("/notificacao-novo-participante-grupo")
          .contentType(MediaType.APPLICATION_JSON)
          .body(Map.of("email", email, "nomeGrupo", nomeGrupo, "nomeParticipante", nomeParticipante))
          .retrieve()
          .toBodilessEntity();
      log.info(
          "[REQUISIÇÃO][EmailService] - Notificação de novo participante enviada com sucesso para: {}",
          email);
      return true;
    } catch (Exception ex) {
      log.error(
          "[REQUISIÇÃO][EmailService] - Falha ao notificar administrador do grupo '{}' ({}): {}",
          nomeGrupo,
          email,
          ex.getMessage());
      return false;
    }
  }

  public boolean notificarDenunciaGrupo(String email, String nomeGrupo, long qtdeDenuncias) {
    log.info(
        "[REQUISIÇÃO][EmailService] - Solicitando notificação de denúncias do grupo '{}' para admin: {}. Total PENDENTE: {}",
        nomeGrupo,
        email,
        qtdeDenuncias);
    try {
      restClient
          .post()
          .uri("/notificacao-denuncia-grupo")
          .contentType(MediaType.APPLICATION_JSON)
          .body(Map.of("email", email, "nomeGrupo", nomeGrupo, "qtdeDenuncias", qtdeDenuncias))
          .retrieve()
          .toBodilessEntity();
      log.info(
          "[REQUISIÇÃO][EmailService] - Notificação de denúncia do grupo '{}' enviada com sucesso para: {}",
          nomeGrupo,
          email);
      return true;
    } catch (Exception ex) {
      log.error(
          "[REQUISIÇÃO][EmailService] - Falha ao notificar admin sobre denúncias do grupo '{}' ({}): {}",
          nomeGrupo,
          email,
          ex.getMessage());
      return false;
    }
  }
}
