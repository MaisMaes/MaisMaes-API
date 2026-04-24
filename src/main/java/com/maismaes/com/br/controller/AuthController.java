package com.maismaes.com.br.controller;

import com.maismaes.com.br.dto.request.AuthRequestDTO;
import com.maismaes.com.br.dto.request.RecuperarSenhaRequestDTO;
import com.maismaes.com.br.dto.request.RedefinirSenhaRequestDTO;
import com.maismaes.com.br.dto.response.AuthResponseDTO;
import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.service.RecuperacaoSenhaService;
import com.maismaes.com.br.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RecuperacaoSenhaService recuperacaoSenhaService;

    @PostMapping("/login")
    @Operation(
            summary = "Login do usuário",
            description = "Este endpoint permite o usuário acessar sua conta"
    )
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO login) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(login.email(), login.senha());
        var auth = this.authenticationManager.authenticate(usernamePassword);
        var token = tokenService.generateToken((Perfil) auth.getPrincipal());

        var response = new AuthResponseDTO(token);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/recuperar-senha")
    @Operation(
            summary = "Solicita recuperação de senha",
            description = "Envia um código de 6 dígitos para o e-mail informado, caso esteja cadastrado."
    )
    public ResponseEntity<Map<String, String>> recuperarSenha(@RequestBody @Valid RecuperarSenhaRequestDTO request) {
        recuperacaoSenhaService.solicitarRecuperacao(request.email());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("mensagem",
                        "Se o e-mail informado estiver cadastrado, um código de recuperação será enviado."));
    }

    @PostMapping("/redefinir-senha")
    @Operation(
            summary = "Redefine a senha do usuário",
            description = "Valida o código recebido por e-mail e atualiza a senha do usuário."
    )
    public ResponseEntity<Map<String, String>> redefinirSenha(@RequestBody @Valid RedefinirSenhaRequestDTO request) {
        recuperacaoSenhaService.redefinirSenha(request.codigo(), request.novaSenha());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("mensagem", "Senha redefinida com sucesso."));
    }

}
