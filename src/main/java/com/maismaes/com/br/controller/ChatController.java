package com.maismaes.com.br.controller;

import com.maismaes.com.br.dto.response.ChatHistoricoResponseDTO;
import com.maismaes.com.br.entities.ChatMensagem;
import com.maismaes.com.br.repository.ChatMensagemRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {

  private static final int PAGE_SIZE_MAX = 100;

  private final ChatMensagemRepository chatMensagemRepository;

  @MessageMapping("/sendMessage/{groupId}")
  @SendTo("/topic/group/{groupId}")
  public ChatMensagem enviarMensagem(
      @DestinationVariable Long groupId, @Payload ChatMensagem mensagem) {
    log.info(
        "[WebSocket] Mensagem recebida | GroupID: {} | Remetente: {} | Tipo: {}",
        groupId,
        mensagem.getSender(),
        mensagem.getType());
    mensagem.setTimestamp(LocalDateTime.now());
    mensagem.setGroupId(groupId);
    // save() preenche o campo 'id' no objeto retornado — essencial para deduplicação no front
    ChatMensagem salva = chatMensagemRepository.save(mensagem);
    log.info("[WebSocket] Mensagem salva | ID: {} | GroupID: {}", salva.getId(), groupId);
    return salva;
  }

  @GetMapping("/chat/grupos/{groupId}/mensagens")
  public ResponseEntity<ChatHistoricoResponseDTO> buscarHistorico(
      @PathVariable Long groupId,
      @RequestParam(required = false) String antes,
      @RequestParam(defaultValue = "30") int quantidade,
      HttpServletRequest request) {

    int pageSize = Math.min(Math.max(quantidade, 1), PAGE_SIZE_MAX);
    log.info(
        "[Chat] Buscando histórico | GroupID: {} | Cursor: {} | Quantidade: {}",
        groupId,
        antes,
        pageSize);

    PageRequest pageable = PageRequest.of(0, pageSize + 1);

    List<ChatMensagem> resultado;
    if (antes != null) {
      LocalDateTime cursor = LocalDateTime.parse(antes, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      resultado =
          chatMensagemRepository.findByGroupIdAndTimestampBeforeOrderByTimestampDesc(
              groupId, cursor, pageable);
    } else {
      resultado = chatMensagemRepository.findByGroupIdOrderByTimestampDesc(groupId, pageable);
    }

    boolean temMais = resultado.size() > pageSize;
    List<ChatMensagem> pagina = temMais ? resultado.subList(0, pageSize) : resultado;

    List<ChatMensagem> mensagensOrdenadas = new ArrayList<>(pagina);
    Collections.reverse(mensagensOrdenadas);

    String proximoCursor = null;
    String proximaUrl = null;

    if (temMais) {
      proximoCursor =
          mensagensOrdenadas.get(0).getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      String baseUrl = request.getRequestURL().toString();
      proximaUrl =
          baseUrl
              + "?quantidade="
              + pageSize
              + "&antes="
              + URLEncoder.encode(proximoCursor, StandardCharsets.UTF_8);
    }

    log.info(
        "[Chat] Histórico retornado | GroupID: {} | Qtd: {} | TemMais: {}",
        groupId,
        mensagensOrdenadas.size(),
        temMais);

    return ResponseEntity.ok(
        new ChatHistoricoResponseDTO(mensagensOrdenadas, proximoCursor, proximaUrl, temMais));
  }
}
