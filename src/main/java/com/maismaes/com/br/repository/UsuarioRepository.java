package com.maismaes.com.br.repository;

import com.maismaes.com.br.entities.Usuario;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
  Optional<Usuario> findByPerfil_PerfilEmail(String perfilEmail);

  boolean existsByEmailAndIdNot(String email, UUID id);

  boolean existsByTelefoneAndIdNot(String telefone, UUID id);
}
