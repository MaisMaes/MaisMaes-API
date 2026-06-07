package com.maismaes.com.br.repository;

import com.maismaes.com.br.entities.Infocard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InfocardRepository extends JpaRepository<Infocard, UUID> {

    List<Infocard> findByAtivoTrue();

    List<Infocard> findByDestaqueTrueAndAtivoTrue();

    List<Infocard> findByAtivoTrueOrderByDataCriacaoDesc();

    List<Infocard> findByDestaqueTrueAndAtivoTrueOrderByDataCriacaoDesc();

    Page<Infocard> findByAtivo(
            Boolean ativo,
            Pageable pageable
    );

    Page<Infocard> findByTituloContainingIgnoreCaseAndAtivoTrue(
            String titulo,
            Pageable pageable
    );

    Page<Infocard> findByAtivoTrue(Pageable pageable);

    Page<Infocard> findByDestaqueTrueAndAtivoTrue(Pageable pageable);
}