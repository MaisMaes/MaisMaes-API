package com.maismaes.com.br.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.maismaes.com.br.dto.request.AtualizaDto;
import com.maismaes.com.br.dto.request.CadastroInfocardDto;
import com.maismaes.com.br.dto.response.InfoCardResponseDto;
import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.entities.Role;
import com.maismaes.com.br.service.InfocardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/infocards")
@RequiredArgsConstructor
@Tag(
        name = "Infocards",
        description = "Gerenciamento dos infocards da plataforma"
)
public class InfoCardController {

    private final InfocardService infocardService;

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping
    @Operation(
            summary = "Cria infocard",
            description = "Este método criar um infocard"
    )
    public ResponseEntity<InfoCardResponseDto> cadastrar(
            @RequestBody @Valid CadastroInfocardDto dto,
            @AuthenticationPrincipal Perfil perfilLogado) {

        if (perfilLogado.getRole() != Role.ADMINISTRADOR) {
            throw new RuntimeException("Acesso negado");
        }

        InfoCardResponseDto response =
                infocardService.cadastrar(dto, perfilLogado.getUsuario());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Busca infocard por id",
            description = "Este método busca um infocard por id "
    )
    public ResponseEntity<InfoCardResponseDto> buscarPorId(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                infocardService.buscarPorId(id)
        );
    }


    @Operation(
            summary = "Lista infocards",
            description = "Este método lista todos infocards, pode receber parametros para personalizar busca"
    )
    @GetMapping
    public ResponseEntity<Page<InfoCardResponseDto>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho,
            @RequestParam(required = false) Boolean ativo) {

        return ResponseEntity.ok(
                infocardService.listar(
                        pagina,
                        tamanho,
                        ativo
                )
        );
    }


    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/{id}")
    @Operation(
            summary = "Atualização de um infocard",
            description = "Este método atualiza um infocard, pode ser atualizacao parcial ou total"
    )
    public ResponseEntity<InfoCardResponseDto> atualizar(
            @PathVariable UUID id,
            @RequestBody AtualizaDto dto,
            @AuthenticationPrincipal Perfil perfilLogado) {

        if (perfilLogado.getRole() != Role.ADMINISTRADOR) {
            throw new RuntimeException("Acesso negado");
        }

        return ResponseEntity.ok(
                infocardService.atualizar(id, dto)
        );
    }


    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Deleta um infocard",
            description = "Um usuario adm pode deletar um infocard"
    )
    public ResponseEntity<Void> deletar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Perfil perfilLogado) {

        if (perfilLogado.getRole() != Role.ADMINISTRADOR) {
            throw new RuntimeException("Acesso negado");
        }

        infocardService.deletar(id);

        return ResponseEntity.noContent().build();
    }



    //DESTAQUE

    @GetMapping("/destaques")
    @Operation(
            summary = "Lista inforcards em destaque",
            description = "O usuario adm pode promover inforcards a destaque, esse metodos os lista"
    )
    public ResponseEntity<Page<InfoCardResponseDto>> listarDestaques(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {

        return ResponseEntity.ok(
                infocardService.listarDestaques(
                        pagina,
                        tamanho
                )
        );
    }


    //ROTAS  FAVORITAR

    @PostMapping("/{id}/favoritos")
    @Operation(
            summary = "Favorita um infocard",
            description = "Um usuário pode adicionar um infocard a seus favoritos "
    )
    public ResponseEntity<Void> favoritar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Perfil perfilLogado) {

        infocardService.adicionarFavorito(
                perfilLogado.getUsuario().getId(),
                id
        );

        return ResponseEntity.ok().build();
    }

@DeleteMapping("/{id}/favoritos")
@Operation(
        summary = "Remove um infocard dos favoritos",
        description = "Remove um infocard da lista de favoritos do usuário autenticado."
)
public ResponseEntity<Void> removerFavorito(
        @PathVariable UUID id,
        @AuthenticationPrincipal Perfil perfilLogado) {

    infocardService.removerFavorito(
            perfilLogado.getUsuario().getId(),
            id
    );

    return ResponseEntity.noContent().build();
}


    @Operation(
            summary = "Listar favoritos",
            description = "Retorna uma lista paginada dos infocards favoritados pelo usuário autenticado."
    )
    @GetMapping("/favoritos")
    public ResponseEntity<Page<InfoCardResponseDto>> listarFavoritos(
            @AuthenticationPrincipal Perfil perfilLogado,

            @Parameter(
                    description = "Número da página",
                    example = "0"
            )
            @RequestParam(defaultValue = "0")
            int pagina,

            @Parameter(
                    description = "Quantidade de registros por página",
                    example = "10"
            )
            @RequestParam(defaultValue = "10")
            int tamanho) {

        return ResponseEntity.ok(
                infocardService.listarFavoritos(
                        perfilLogado.getUsuario().getId(),
                        pagina,
                        tamanho
                )
        );
    }



    //BUSCAR INFOCARD POR TITULO

    @GetMapping("/buscar")
    @Operation(
            summary = "faz uma pesquisa por titulo",
            description = "Remove um infocard da lista de favoritos do usuário autenticado."
    )
    public ResponseEntity<Page<InfoCardResponseDto>> buscarPorTitulo(
            @RequestParam String titulo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {

        return ResponseEntity.ok(
                infocardService.buscarPorTitulo(
                        titulo,
                        pagina,
                        tamanho
                )
        );
    }



}
