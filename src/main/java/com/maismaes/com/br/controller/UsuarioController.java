package com.maismaes.com.br.controller;

import com.maismaes.com.br.dto.request.BuscaDadosContaResponseDTO;
import com.maismaes.com.br.dto.request.CadastroUsuarioRequestDTO;
import com.maismaes.com.br.dto.response.CadastroUsuarioResponseDTO;
import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.service.TokenService;
import com.maismaes.com.br.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("usuario")
public class UsuarioController {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UsuarioService usuarioService;
    private final TokenService tokenService;

    @PostMapping("/cadastro")
    @Operation(
            summary = "Registra usuário",
            description = "Este endpoint cria um novo usuário"
    )
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


    @GetMapping("/me")
    @Operation(
            summary = "Trás informações do usuário logado",
            description = "Este endpoint retorna informações da conta do usuário logado, para isso, o usuário terá que estar autenticado"
    )
    public ResponseEntity<BuscaDadosContaResponseDTO> buscarMinhaConta(
            @AuthenticationPrincipal Perfil perfil
    ) {
        String perfil_ = perfil.getPerfilEmail();

        BuscaDadosContaResponseDTO response =
                usuarioService.buscarDadosConta(perfil_);
        return ResponseEntity.ok(response);
    }


}
