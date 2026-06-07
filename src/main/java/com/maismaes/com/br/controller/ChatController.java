package com.maismaes.com.br.controller;

import com.maismaes.com.br.entities.ChatMensagem;
import com.maismaes.com.br.repository.ChatMensagemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatMensagemRepository chatMensagemRepository;
    private final GridFsTemplate gridFsTemplate;

    @MessageMapping("/sendMessage/{groupId}")
    @SendTo("/topic/group/{groupId}")
    public ChatMensagem enviarMensagem(@DestinationVariable Long groupId, ChatMensagem mensagem){
        log.info("[WebSocket] Mensagem recebida | GroupID: {} | Remetente: {}", groupId, mensagem.getSender());
        mensagem.setTimestamp(LocalDateTime.now());
        mensagem.setGroupId(groupId);
        chatMensagemRepository.save(mensagem);
        log.info("[WebSocket] Mensagem salva e enviada para /topic/group/{}", groupId);
        return mensagem;
    }

    @PostMapping("/upload")
    public String uploadArquivo(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("[WebSocket] Upload de arquivo iniciado | Nome: {} | Tipo: {} | Tamanho: {} bytes",
                file.getOriginalFilename(), file.getContentType(), file.getSize());
        ObjectId id = gridFsTemplate.store(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType()
        );
        log.info("[WebSocket] Upload concluído | GridFS ID: {}", id);
        return id.toString();
    }

}
