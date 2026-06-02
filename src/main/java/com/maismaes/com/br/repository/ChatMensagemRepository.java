package com.maismaes.com.br.repository;

import com.maismaes.com.br.entities.ChatMensagem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMensagemRepository extends MongoRepository<ChatMensagem, String> {
    List<ChatMensagem> findBySender(String sender);
}
