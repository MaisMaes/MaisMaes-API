package com.maismaes.com.br.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "chat_mensagens")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMensagem {

    @Id
    private String id;
    @Indexed
    private Long groupId;
    private String sender;
    private String content;
    private MessageType type;
    @Indexed
    private LocalDateTime timestamp;

    // Campos de arquivo — presentes apenas em mensagens do tipo FILE/AUDIO/IMAGE
    private String fileId;
    private String fileName;
    private String mimeType;
}


