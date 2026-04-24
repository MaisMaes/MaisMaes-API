package com.maismaes.com.br.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.maismaes.com.br.entities.grupo_tematico.GrupoTematico;

public interface GrupoTematicoRepository extends JpaRepository<GrupoTematico, Long>, JpaSpecificationExecutor<GrupoTematico>{ 

}
