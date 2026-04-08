package com.maismaes.com.br.entities;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Builder
@Getter
@Setter
@Entity
@Table(name = "perfis")
@NoArgsConstructor
@AllArgsConstructor
public class Perfil implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String perfilEmail;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.MAE_SOLO;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == Role.ADMINISTRADOR){
            return List.of(
                    new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")
            );
        }else {
            return List.of(
                    new SimpleGrantedAuthority("ROLE_MAE_SOLO")
            );
        }
    }

    @Override
    public String getUsername() {
        return perfilEmail;
    }

    @Override
    public String getPassword() {
        return senha;
    }
}
