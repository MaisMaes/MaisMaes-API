package com.maismaes.com.br.exception;

public class SenhaIgualException extends RuntimeException {
    public SenhaIgualException() {
        super("A senha não pode ser igual a anterior !");
    }

    public SenhaIgualException(String message) {
        super(message);
    }


}
