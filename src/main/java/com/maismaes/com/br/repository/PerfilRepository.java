package com.maismaes.com.br.repository;

import com.maismaes.com.br.entities.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfilRepository extends JpaRepository<Perfil, Long>{

    Perfil findByPerfilEmail(String email);

}
