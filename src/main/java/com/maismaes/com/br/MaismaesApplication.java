package com.maismaes.com.br;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class MaismaesApplication {

  public static void main(String[] args) {
    SpringApplication.run(MaismaesApplication.class, args);
  }
}
