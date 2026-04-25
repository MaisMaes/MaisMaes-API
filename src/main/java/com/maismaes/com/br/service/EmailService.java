package com.maismaes.com.br.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from:nao-responda@maismaes.com.br}")
    private String from;

    @Async
    public void enviarCodigoRecuperacao(String destinatario, String codigo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(destinatario);
        message.setSubject("Mais Mães - Código de recuperação de senha");
        message.setText(
                "Olá!\n\n" +
                "Recebemos uma solicitação para redefinir a sua senha.\n" +
                "Use o código abaixo para concluir o processo:\n\n" +
                "    " + codigo + "\n\n" +
                "Este código expira em alguns minutos. Caso não tenha solicitado, ignore esta mensagem.\n\n" +
                "Equipe Mais Mães."
        );
        mailSender.send(message);
    }
}

