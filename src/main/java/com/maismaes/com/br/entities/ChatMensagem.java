package com.maismaes.com.br.entities;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMensagem {

    @Id
    private String id;
    private Long groupId;
    private String sender;
    private String content;
    private MessageType type;
    private LocalDateTime timestamp;

}
