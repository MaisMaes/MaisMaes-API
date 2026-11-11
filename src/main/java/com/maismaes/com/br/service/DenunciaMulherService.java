package com.maismaes.com.br.service;

import com.maismaes.com.br.dto.request.CriarDenunciaMulherRequestDTO;
import com.maismaes.com.br.dto.response.DenunciaMulherResponseDTO;
import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.entities.Usuario;
import com.maismaes.com.br.entities.denuncia.DenunciaMulher;
import com.maismaes.com.br.entities.denuncia.StatusDenunciaMulher;
import com.maismaes.com.br.repository.DenunciaMulherRepository;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class DenunciaMulherService {

  private final DenunciaMulherRepository denunciaMulherRepository;

  @Transactional
  public DenunciaMulherResponseDTO criarDenuncia(Perfil perfilLogado) {
    log.info(
        "[DenunciaMulherService] criarDenuncia - Iniciando criação de denúncia para usuário: {}",
        perfilLogado.getUsuario().getId());

    Usuario usuario = perfilLogado.getUsuario();

    DenunciaMulher denuncia =
        DenunciaMulher.builder()
            .usuario(usuario)
            .nomeUsuario(usuario.getNome())
            .email(usuario.getEmail())
            .endereco("")
            .contato(usuario.getTelefone())
            .status(StatusDenunciaMulher.ABERTA)
            .descricao("")
            .build();

    DenunciaMulher denunciaSalva = denunciaMulherRepository.save(denuncia);
    log.info(
        "[DenunciaMulherService] criarDenuncia - Denúncia criada com sucesso. ID: {}",
        denunciaSalva.getId());

    return DenunciaMulherResponseDTO.fromEntity(denunciaSalva);
  }

  public Page<DenunciaMulherResponseDTO> listarDenuncias(UUID usuarioId, int pagina, int tamanho) {
    log.info(
        "[DenunciaMulherService] listarDenuncias - Listando denúncias do usuário: {} - Página: {}, Tamanho: {}",
        usuarioId,
        pagina,
        tamanho);

    Pageable pageable =
        PageRequest.of(pagina, tamanho, Sort.by("dataDenuncia").descending());

    Page<DenunciaMulher> denuncias = denunciaMulherRepository.findByUsuarioId(usuarioId, pageable);

    return denuncias.map(DenunciaMulherResponseDTO::fromEntity);
  }

  @Transactional
  public DenunciaMulherResponseDTO atualizarStatus(Long denunciaId, StatusDenunciaMulher novoStatus) {
    log.info(
        "[DenunciaMulherService] atualizarStatus - Atualizando status da denúncia ID: {} para: {}",
        denunciaId,
        novoStatus);

    DenunciaMulher denuncia =
        denunciaMulherRepository
            .findById(denunciaId)
            .orElseThrow(() -> new RuntimeException("Denúncia não encontrada"));

    denuncia.setStatus(novoStatus);
    DenunciaMulher denunciaAtualizada = denunciaMulherRepository.save(denuncia);

    log.info(
        "[DenunciaMulherService] atualizarStatus - Status atualizado com sucesso");

    return DenunciaMulherResponseDTO.fromEntity(denunciaAtualizada);
  }
}
