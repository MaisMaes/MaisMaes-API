package com.maismaes.com.br.service;

import com.maismaes.com.br.dto.request.AtualizaDadosContaDTO;
import com.maismaes.com.br.dto.request.BuscaDadosContaResponseDTO;
import com.maismaes.com.br.dto.request.DeletaContaDTO;
import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.entities.Usuario;
import com.maismaes.com.br.exception.SenhaException;
import com.maismaes.com.br.exception.UsuarioNaoEncontradoException;
import com.maismaes.com.br.repository.PerfilRepository;
import com.maismaes.com.br.repository.UsuarioRepository;
import com.maismaes.com.br.utils.UserValidationUtils;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UsuarioService {

  private final UsuarioRepository usuarioRepository;
  private final UserValidationUtils userValidationUtils;
  private final PerfilRepository perfilRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  public Usuario cadastrarUsuario(Usuario usuario) {
    userValidationUtils.verificarUnicidade(
        usuario.getEmail(), usuario.getTelefone(), usuario.getId());
    return usuarioRepository.save(usuario);
  }

  public BuscaDadosContaResponseDTO buscarDadosConta(String user_email) {
    Usuario usuario =
        usuarioRepository
            .findByPerfil_PerfilEmail(user_email)
            .orElseThrow(UsuarioNaoEncontradoException::new);
    return new BuscaDadosContaResponseDTO(usuario);
  }

  @Transactional // Importante para garantir a persistência  checking
  public BuscaDadosContaResponseDTO atualizaDadosConta(AtualizaDadosContaDTO dto, UUID id) {
    Usuario usuario =
        usuarioRepository.findById(id).orElseThrow(UsuarioNaoEncontradoException::new);

    Perfil perfil =
        Optional.ofNullable(usuario.getPerfil())
            .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

    userValidationUtils.verificarUnicidade(dto.email(), dto.telefone(), usuario.getId());

    if (isValid(dto.nome())) {
      usuario.setNome(dto.nome());
    }

    if (isValid(dto.telefone())) {
      usuario.setTelefone(dto.telefone());
    }

    if (isValid(dto.email())) {
      usuario.setEmail(dto.email());
      perfil.setPerfilEmail(dto.email());
    }

    if (isValid(dto.senha())) {

      if (bCryptPasswordEncoder.matches(dto.senha(), perfil.getSenha())) {
        throw new SenhaException("Senha igual a anterior!");
      }
      perfil.setSenha(bCryptPasswordEncoder.encode(dto.senha()));
    }

    return new BuscaDadosContaResponseDTO(usuario);
  }

  private boolean isValid(String value) {
    return value != null && !value.isBlank();
  }

  @Transactional
  public void deletaConta(Perfil perfilLogado, DeletaContaDTO dto) {

    Usuario usuario = perfilLogado.getUsuario();

    if (usuario == null) {
      throw new RuntimeException("Usuário não encontrado");
    }

    if (!bCryptPasswordEncoder.matches(dto.senha(), perfilLogado.getPassword())) {
      throw new SenhaException("Senha inválida!");
    }

    usuario.setPerfil(null);
    perfilLogado.setUsuario(null);

    usuarioRepository.delete(usuario);
    perfilRepository.delete(perfilLogado);
  }
}
