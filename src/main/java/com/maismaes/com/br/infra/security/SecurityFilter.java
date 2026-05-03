package com.maismaes.com.br.infra.security;

import com.maismaes.com.br.entities.Perfil;
import com.maismaes.com.br.repository.PerfilRepository;
import com.maismaes.com.br.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Component
public class SecurityFilter extends OncePerRequestFilter {

  private final TokenService tokenService;
  private final PerfilRepository perfilRepository;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var token = this.recoverToken(request); // Recupera o token da requisição
    if (token != null) { // Se houver um token presente
      String email =
          tokenService.validateToken(token); // Valida o token e extrai o email do usuário
      Perfil user = perfilRepository.findByPerfilEmail(email); // Busca o usuário no banco de dados.

      if (user == null) { //
        filterChain.doFilter(request, response);
        return;
      }

      // Cria um objeto de autenticação do Spring Security com o usuário autenticado
      var authentication =
          new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

      // Define o usuário autenticado no contexto de segurança da aplicação
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // Continua a cadeia de filtros, permitindo que a requisição prossiga
    filterChain.doFilter(request, response);
  }

  private String recoverToken(HttpServletRequest request) {
    var authHeader =
        request.getHeader("Authorization"); // Obtém o cabeçalho "Authorization" da requisição
    if (authHeader == null) return null; // Se não houver cabeçalho, retorna null
    return authHeader.replace("Bearer ", ""); // Remove o prefixo "Bearer " e retorna apenas o token
  }
}
