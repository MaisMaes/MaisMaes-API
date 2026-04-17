package com.maismaes.com.br.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;


import com.maismaes.com.br.dto.request.GrupoTematicoRequestDTO;
import com.maismaes.com.br.dto.response.GrupoTematicoResponseDTO;
import com.maismaes.com.br.service.GrupoTematicoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("grupo-tematico")
public class GrupoTematicoController {

    private final GrupoTematicoService grupoTematicoService;

    @PostMapping("/criar")
    public ResponseEntity<GrupoTematicoResponseDTO> criarGrupoTematico(@RequestBody @Valid GrupoTematicoRequestDTO grupoTematicoRequestDTO){
        var grupoTematicoCriado = grupoTematicoService.criarGrupoTematico(grupoTematicoRequestDTO.ToGrupoTematicoEntity());

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(new GrupoTematicoResponseDTO());
       
    }

}
