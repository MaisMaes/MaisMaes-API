package com.maismaes.com.br.controller;

import com.maismaes.com.br.dto.request.AuthRequestDTO;
import com.maismaes.com.br.dto.response.AuthResponseDTO;
import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO login) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(login.email(), login.senha());
        var auth = this.authenticationManager.authenticate(usernamePassword);
        var token = tokenService.generateToken((Perfil) auth.getPrincipal());

        var response = new AuthResponseDTO(token);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

}
