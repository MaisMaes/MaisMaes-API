package com.maismaes.com.br.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class KafkaProducerEmailService {

    private final KafkaTemplate<String, Map<String,String>> kafkaTemplate;

    public KafkaProducerEmailService(KafkaTemplate<String, Map<String,String>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(Map<String,String> message) {
        kafkaTemplate.send("topico-emails", message);
    }
}
