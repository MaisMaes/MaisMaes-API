package com.maismaes.com.br.service;

import java.util.Map;
import java.util.UUID;

import com.maismaes.com.br.kafka.KafkaProducerEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  private final KafkaProducerEmailService kafkaProducerMailService;

  @Value("${mail.from:nao-responda@maismaes.com.br}")
  private String from;

  public boolean enviarCodigoRecuperacao(String destinatario, String codigo) {
    log.info(
        "[KAFKA][EmailService] - Publicando mensagem de recuperação de senha para o tópico 'email'");
    try {
      Map<String, String> payload = Map.of(
          "email", destinatario,
          "codigo", codigo,
          "tipo", "recuperar-senha",
          "strategy", "recuperar-senha");
      kafkaProducerMailService.sendMessage(payload);
      log.info(
          "[KAFKA][EmailService] - Código de recuperação de senha publicado com sucesso para: {}",
          destinatario);
      return true;
    } catch (Exception ex) {
      log.error(
          "[KAFKA][EmailService] - Falha ao publicar código de recuperação de senha para: {}",
          destinatario,
          ex);
      return false;
    }
  }

  public boolean notificarNovoParticipante(
      String email, String nomeGrupo, String nomeParticipante) {
    log.info(
        "[KAFKA][EmailService] - Publicando notificação de novo participante no grupo '{}' para: {}",
        nomeGrupo,
        email);
    try {
      Map<String, String> payload = Map.of(
          "email", email,
          "nomeGrupo", nomeGrupo,
          "nomeParticipante", nomeParticipante,
          "tipo", "notificacao-novo-participante-grupo",
          "strategy", "novo-participante");
      kafkaProducerMailService.sendMessage(payload);
      log.info(
          "[KAFKA][EmailService] - Notificação de novo participante publicada com sucesso para: {}",
          email);
      return true;
    } catch (Exception ex) {
      log.error(
          "[KAFKA][EmailService] - Falha ao publicar notificação de novo participante no grupo '{}' ({}): {}",
          nomeGrupo,
          email,
          ex.getMessage());
      return false;
    }
  }

  public boolean notificarDenunciaGrupo(String email, String nomeGrupo, long qtdeDenuncias) {
    log.info(
        "[KAFKA][EmailService] - Publicando notificação de denúncias do grupo '{}' para admin: {}. Total PENDENTE: {}",
        nomeGrupo,
        email,
        qtdeDenuncias);
    try {
      Map<String, String> payload = Map.of(
          "email", email,
          "nomeGrupo", nomeGrupo,
          "qtdeDenuncias", String.valueOf(qtdeDenuncias),
          "tipo", "notificacao-denuncia-grupo",
          "strategy", "denuncia-grupo");
      kafkaProducerMailService.sendMessage(payload);
      log.info(
          "[KAFKA][EmailService] - Notificação de denúncia do grupo '{}' publicada com sucesso para: {}",
          nomeGrupo,
          email);
      return true;
    } catch (Exception ex) {
      log.error(
          "[KAFKA][EmailService] - Falha ao publicar notificação de denúncias do grupo '{}' ({}): {}",
          nomeGrupo,
          email,
          ex.getMessage());
      return false;
    }
  }

  public boolean enviarEmailDeAtivacao(String email, UUID idUsuario) {
    log.info(
        "[KAFKA][EmailService] - Publicando mensagem de ativação de conta para o tópico 'email'");
    try {
      Map<String, String> payload = Map.of(
          "email", email,
          "idUsuario", idUsuario.toString(),
          "tipo", "ativacao-conta",
          "strategy", "ativar-conta");
      kafkaProducerMailService.sendMessage(payload);
      log.info(
          "[KAFKA][EmailService] - Email de ativação de conta publicado com sucesso para: {}",
          email);
      return true;
    } catch (Exception ex) {
      log.error(
          "[KAFKA][EmailService] - Falha ao publicar email de ativação de conta para: {}",
          email,
          ex);
      return false;
    }
  }
}
