package com.maismaes.com.br.service;

import com.maismaes.com.br.dto.request.BuscaDadosContaResponseDTO;
import com.maismaes.com.br.entities.Usuario;
import com.maismaes.com.br.exception.UsuarioNaoEncontradoException;
import com.maismaes.com.br.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public Usuario cadastrarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }


    public BuscaDadosContaResponseDTO buscarDadosConta(String user_email) {
        Usuario usuario = usuarioRepository.findByPerfil_PerfilEmail(user_email)
                .orElseThrow(UsuarioNaoEncontradoException::new);

        return new BuscaDadosContaResponseDTO(
                usuario.getNome(),
                usuario.getPerfil().getUsername(),
                usuario.getTelefone(),
                usuario.getPerfil().getRole().name()
        );
    }



}
