package com.maismaes.com.br.controller;

import com.maismaes.com.br.dto.request.CadastroUsuarioRequestDTO;
import com.maismaes.com.br.dto.response.CadastroUsuarioResponseDTO;
import com.maismaes.com.br.service.TokenService;
import com.maismaes.com.br.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("usuario")
public class UsuarioController {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UsuarioService usuarioService;
    private final TokenService tokenService;

    @PostMapping("/cadastro")
    public ResponseEntity<CadastroUsuarioResponseDTO> cadastrarUsuario(@RequestBody @Valid CadastroUsuarioRequestDTO cadastroUsuarioRequestDTO){
        var senhaEncriptada = bCryptPasswordEncoder.encode(cadastroUsuarioRequestDTO.senha());

        var novoUsuario = usuarioService.cadastrarUsuario(cadastroUsuarioRequestDTO
                .toUsuarioEntity(senhaEncriptada));

        var token = tokenService.generateToken(novoUsuario.getPerfil());
        var response = new CadastroUsuarioResponseDTO(token);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
