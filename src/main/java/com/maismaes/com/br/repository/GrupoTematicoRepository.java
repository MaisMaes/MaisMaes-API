package com.maismaes.com.br.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import com.maismaes.com.br.entities.grupo_tematico.Categoria;
import com.maismaes.com.br.entities.grupo_tematico.GrupoTematico;

public interface GrupoTematicoRepository extends JpaRepository<GrupoTematico, Long>, JpaSpecificationExecutor<GrupoTematico>{ 

    List<GrupoTematico> findByTituloContainingIgnoreCase(String titulo);

    List<GrupoTematico> findByCategorias(Categoria categorias);

    @Query("SELECT DISTINCT g FROM GrupoTematico g JOIN g.bairros b WHERE LOWER(b.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<GrupoTematico> buscarPorNomeBairro(@Param("nome") String nome);

    @EntityGraph(attributePaths = {"bairros", "participantes"})
    Optional<GrupoTematico> findById(Long id);
}
