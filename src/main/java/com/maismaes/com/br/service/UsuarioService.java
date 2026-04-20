package com.maismaes.com.br.service;

import com.maismaes.com.br.dto.request.AtualizaDadosContaDTO;
import com.maismaes.com.br.dto.request.BuscaDadosContaResponseDTO;
import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.entities.Usuario;
import com.maismaes.com.br.exception.SenhaIgualException;
import com.maismaes.com.br.exception.UsuarioNaoEncontradoException;
import com.maismaes.com.br.repository.PerfilRepository;
import com.maismaes.com.br.repository.UsuarioRepository;
import com.maismaes.com.br.utils.UserValidationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UserValidationUtils userValidationUtils;
    private final PerfilRepository perfilRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    public Usuario cadastrarUsuario(Usuario usuario) {
        userValidationUtils.verificarUnicidade(usuario.getEmail(), usuario.getTelefone(), usuario.getId());
        return usuarioRepository.save(usuario);
    }


    public BuscaDadosContaResponseDTO buscarDadosConta(String user_email) {
        Usuario usuario = usuarioRepository.findByPerfil_PerfilEmail(user_email)
                .orElseThrow(UsuarioNaoEncontradoException::new);
        return new BuscaDadosContaResponseDTO(usuario);
    }

    @Transactional // Importante para garantir a persistência  checking
    public BuscaDadosContaResponseDTO atualizaDadosConta(AtualizaDadosContaDTO dto, UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(UsuarioNaoEncontradoException::new);

        Perfil perfil = Optional.ofNullable(usuario.getPerfil())
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

        userValidationUtils.verificarUnicidade(
                dto.email(),
                dto.telefone(),
                usuario.getId()
        );

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
                throw new SenhaIgualException();
            }
            perfil.setSenha(bCryptPasswordEncoder.encode(dto.senha()));
        }

        return new BuscaDadosContaResponseDTO(usuario);
    }

    private boolean isValid(String value) {
        return value != null && !value.isBlank();
    }


    public void deletaUsuario(){

    }













}
