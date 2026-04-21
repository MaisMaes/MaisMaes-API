package com.maismaes.com.br.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Objects;

@RestControllerAdvice
public class GeneralExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<HashMap<String, String>> handleConstraintDeclarationException(MethodArgumentNotValidException e){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(montaMensagemErro(Objects.requireNonNull(e.getFieldError()).getDefaultMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<HashMap<String, String>> handleAuthenticationException(AuthenticationException e){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(montaMensagemErro("Usuário ou senha incorretos"));
    }

    private HashMap<String, String> montaMensagemErro(String mensagem) {
        var response = new HashMap<String, String>();
        response.put("error", mensagem);
        response.put("horario", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        return response;
    }


    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<HashMap<String, String>> handleUsuarioNaoEncontradoException(
            UsuarioNaoEncontradoException e
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(montaMensagemErro(e.getMessage()));
    }

    @ExceptionHandler(SenhaException.class)
    public ResponseEntity<HashMap<String, String>>
    handleSenhaIgualException(
            SenhaException e
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(montaMensagemErro(e.getMessage()));
    }


    @ExceptionHandler(VerificarUnicidadeException.class)
    public ResponseEntity<HashMap<String,String>>
    handleVerificarUnicidadeException(VerificarUnicidadeException e){
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(montaMensagemErro(e.getMessage()));
    }








}
