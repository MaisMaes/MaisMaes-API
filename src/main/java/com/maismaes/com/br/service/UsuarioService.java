package com.maismaes.com.br.service;

import com.maismaes.com.br.dto.request.AtualizaDadosContaDTO;
import com.maismaes.com.br.dto.request.BuscaDadosContaResponseDTO;
import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.entities.Usuario;
import com.maismaes.com.br.exception.SenhaIgualException;
import com.maismaes.com.br.exception.UsuarioNaoEncontradoException;
import com.maismaes.com.br.repository.PerfilRepository;
import com.maismaes.com.br.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    public Usuario cadastrarUsuario(Usuario usuario) {
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

        Perfil perfil = usuario.getPerfil();

        if (dto.nome() != null && !dto.nome().isBlank()) {
            usuario.setNome(dto.nome());
        }

        if (dto.telefone() != null && !dto.telefone().isBlank()) {
            usuario.setTelefone(dto.telefone());
        }

        if (dto.email() != null && !dto.telefone().isBlank()){
            usuario.setEmail(dto.email());
            perfil.setPerfilEmail(dto.email());
        }

        if (dto.senha() != null && !dto.senha().isBlank()) {

            if (bCryptPasswordEncoder.matches(dto.senha(), perfil.getSenha())) {
                throw new SenhaIgualException();
            }

            var senhaNovaHash = bCryptPasswordEncoder.encode(dto.senha());
            perfil.setSenha(senhaNovaHash);
        }

        return new BuscaDadosContaResponseDTO(usuario);

    }











}
