package com.maismaes.com.br.repository;

import com.maismaes.com.br.entities.ChatMensagem;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatMensagemRepository extends MongoRepository<ChatMensagem, String> {
  List<ChatMensagem> findBySender(String sender);

  List<ChatMensagem> findByGroupId(Long groupId);

  List<ChatMensagem> findByGroupIdOrderByTimestampDesc(Long groupId, Pageable pageable);

  List<ChatMensagem> findByGroupIdAndTimestampBeforeOrderByTimestampDesc(
      Long groupId, LocalDateTime timestamp, Pageable pageable);
}
