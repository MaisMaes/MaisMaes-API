package com.maismaes.com.br.service;


import com.maismaes.com.br.dto.request.AtualizaDto;
import com.maismaes.com.br.dto.request.CadastroInfocardDto;
import com.maismaes.com.br.dto.response.InfoCardResponseDto;
import com.maismaes.com.br.entities.Infocard;
import com.maismaes.com.br.entities.Usuario;
import com.maismaes.com.br.repository.InfocardRepository;
import com.maismaes.com.br.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InfocardService {


    private final InfocardRepository infocardRepository;
    private final UsuarioRepository usuarioRepository;

    public InfoCardResponseDto cadastrar(
            CadastroInfocardDto dto,
            Usuario criador) {

        Infocard infocard = dto.toEntity(criador);

        infocardRepository.save(infocard);

        return new InfoCardResponseDto(infocard);
    }


    public InfoCardResponseDto buscarPorId(UUID id) {

        Infocard infocard = infocardRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Infocard não encontrado"));

        return new InfoCardResponseDto(infocard);
    }


    public Page<InfoCardResponseDto> listar(
            int pagina,
            int tamanho,
            Boolean ativo) {

        Pageable pageable = PageRequest.of(
                pagina,
                tamanho,
                Sort.by("dataCriacao").descending()
        );

        Page<Infocard> infocards;

        if (ativo == null) {
            infocards = infocardRepository.findAll(pageable);
        } else {
            infocards = infocardRepository.findByAtivo(ativo, pageable);
        }

        return infocards.map(InfoCardResponseDto::new);
    }




    public Page<InfoCardResponseDto> buscarPorTitulo(
            String titulo,
            int pagina,
            int tamanho) {

        Pageable pageable = PageRequest.of(
                pagina,
                tamanho,
                Sort.by("dataCriacao").descending()
        );

        return infocardRepository
                .findByTituloContainingIgnoreCaseAndAtivoTrue(
                        titulo,
                        pageable
                )
                .map(InfoCardResponseDto::new);
    }



    public InfoCardResponseDto atualizar(
            UUID id,
            AtualizaDto dto) {

        Infocard infocard = infocardRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Infocard não encontrado"));

        if (dto.titulo() != null && !dto.titulo().isBlank()) {
            infocard.setTitulo(dto.titulo());
        }

        if (dto.descricao() != null && !dto.descricao().isBlank()) {
            infocard.setDescricao(dto.descricao());
        }

        if (dto.imagem() != null && !dto.imagem().isBlank()) {
            infocard.setImagem(dto.imagem());
        }

        if (dto.link() != null && !dto.link().isBlank()) {
            infocard.setLink(dto.link());
        }

        if (dto.destaque() != null) {
            infocard.setDestaque(dto.destaque());
        }

        if (dto.ativo() != null) {
            infocard.setAtivo(dto.ativo());
        }

        infocardRepository.save(infocard);

        return new InfoCardResponseDto(infocard);
    }



    public void deletar(UUID id) {

        if (!infocardRepository.existsById(id)) {
            throw new RuntimeException("Infocard não encontrado");
        }

        infocardRepository.deleteById(id);
    }


    //SERVICO REFERENTE A DESTAQUE:

    public Page<InfoCardResponseDto> listarDestaques(
            int pagina,
            int tamanho) {

        Pageable pageable = PageRequest.of(
                pagina,
                tamanho,
                Sort.by("dataCriacao").descending()
        );

        return infocardRepository
                .findByDestaqueTrueAndAtivoTrue(pageable)
                .map(InfoCardResponseDto::new);
    }




    //SERVICOS RELACIONADOS A FAVORITAR //


    @Transactional
    public void adicionarFavorito(
            UUID usuarioId,
            UUID infocardId) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new RuntimeException("Usuário não encontrado"));

        Infocard infocard = infocardRepository.findById(infocardId)
                .orElseThrow(() ->
                        new RuntimeException("Infocard não encontrado"));

        if (usuario.getFavoritos().contains(infocard)) {
            return;
        }

        usuario.getFavoritos().add(infocard);

        usuarioRepository.save(usuario);
    }

    @Transactional
    public void removerFavorito(
            UUID usuarioId,
            UUID infocardId) {

        Infocard infocard = infocardRepository.findById(infocardId)
                .orElseThrow(() ->
                        new RuntimeException("Infocard não encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new RuntimeException("Usuário não encontrado"));

        usuario.getFavoritos().remove(infocard);

        usuarioRepository.save(usuario);
    }

    @Transactional()
    public Page<InfoCardResponseDto> listarFavoritos(
            UUID usuarioId,
            int pagina,
            int tamanho) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new RuntimeException("Usuário não encontrado"));

        List<InfoCardResponseDto> favoritos = usuario.getFavoritos()
                .stream()
                .map(InfoCardResponseDto::new)
                .toList();

        int inicio = pagina * tamanho;
        int fim = Math.min(inicio + tamanho, favoritos.size());

        List<InfoCardResponseDto> conteudo = favoritos.subList(inicio, fim);

        return new PageImpl<>(
                conteudo,
                PageRequest.of(pagina, tamanho),
                favoritos.size()
        );
    }

}
