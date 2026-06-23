package com.maismaes.com.br.dto.response;

import com.maismaes.com.br.entities.ChatMensagem;
import java.util.List;

public record ChatHistoricoResponseDTO(
    List<ChatMensagem> mensagens, String proximoCursor, String proximaUrl, boolean temMais) {}
