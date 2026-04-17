package com.maismaes.com.br.service;

import org.springframework.stereotype.Service;

import com.maismaes.com.br.entities.grupo_tematico.GrupoTematico;
import com.maismaes.com.br.repository.GrupoTematicoRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class GrupoTematicoService {

    private final GrupoTematicoRepository grupoTematicoRepository;

    public GrupoTematico criarGrupoTematico(GrupoTematico grupoTematico) {
        return grupoTematicoRepository.save(grupoTematico);
    }
}
