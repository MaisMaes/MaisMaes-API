package com.maismaes.com.br.repository;

import com.maismaes.com.br.entities.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PerfilRepository extends JpaRepository<Perfil, UUID>{

    Perfil findByPerfilEmail(String email);



}
