package com.maismaes.com.br.controller;

import com.maismaes.com.br.dto.request.AtualizaDadosContaDTO;
import com.maismaes.com.br.dto.request.BuscaDadosContaResponseDTO;
import com.maismaes.com.br.dto.request.CadastroUsuarioRequestDTO;
import com.maismaes.com.br.dto.request.DeletaContaDTO;
import com.maismaes.com.br.dto.response.CadastroUsuarioResponseDTO;
import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.service.TokenService;
import com.maismaes.com.br.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("usuario")
public class UsuarioController {

  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final UsuarioService usuarioService;
  private final TokenService tokenService;

  @PostMapping("/cadastro")
  @Operation(summary = "Registra usuário", description = "Este endpoint cria um novo usuário")
  public ResponseEntity<CadastroUsuarioResponseDTO> cadastrarUsuario(
      @RequestBody @Valid CadastroUsuarioRequestDTO cadastroUsuarioRequestDTO) {
    log.info("[REQUISIÇÃO] - Chegando requisição de cadastro de usuário");
    var senhaEncriptada = bCryptPasswordEncoder.encode(cadastroUsuarioRequestDTO.senha());

    var novoUsuario =
        usuarioService.cadastrarUsuario(cadastroUsuarioRequestDTO.toUsuarioEntity(senhaEncriptada));

    var token = tokenService.generateToken(novoUsuario.getPerfil());
    var response = new CadastroUsuarioResponseDTO(token);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/me")
  @Operation(
      summary = "Trás informações do usuário logado",
      description =
          "Este endpoint retorna informações da conta do usuário logado, para isso, o usuário terá que estar autenticado")
  public ResponseEntity<BuscaDadosContaResponseDTO> buscarMinhaConta(
      @AuthenticationPrincipal Perfil perfil) {
    String perfil_ = perfil.getPerfilEmail();

    BuscaDadosContaResponseDTO response = usuarioService.buscarDadosConta(perfil_);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/atualizar")
  @Operation(
      summary = "Realiza a atualização dos campos",
      description =
          "Este endpoint faz uma atualização, que pode ser parcial, dos campos do usuario, extrai id do usuário diretamente do token, portanto, o usuário deve estar autenticado.")
  public ResponseEntity<BuscaDadosContaResponseDTO> atualizarDados(
      @AuthenticationPrincipal Perfil perfilLogado, @RequestBody @Valid AtualizaDadosContaDTO dto) {
    BuscaDadosContaResponseDTO response =
        usuarioService.atualizaDadosConta(dto, perfilLogado.getUsuario().getId());
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/deletar/me")
  public ResponseEntity<String> deletarConta(
      @RequestBody DeletaContaDTO dto, @AuthenticationPrincipal Perfil perfilLogado) {

    usuarioService.deletaConta(perfilLogado, dto);

    return ResponseEntity.ok("Conta excluída com sucesso");
  }


  //metodo temporario para promover uma conta a adm
  @PatchMapping("/{id}/promover-admin")
  public ResponseEntity<Void> promoverAdmin(
          @PathVariable UUID id) {

    usuarioService.promoverAdmin(id);

    return ResponseEntity.noContent().build();
  }
}
