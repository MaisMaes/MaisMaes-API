package com.maismaes.com.br.config;

import com.maismaes.com.br.infra.security.SecurityFilter;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final SecurityFilter securityFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            autorize ->
                autorize
                    .requestMatchers("/h2-console/**")
                    .permitAll() // Permite o acesso ao H2 Console
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers("/auth/login")
                    .permitAll()
                    .requestMatchers("/auth/recuperar-senha")
                    .permitAll()
                    .requestMatchers("/auth/redefinir-senha")
                    .permitAll()
                    .requestMatchers("/usuario/cadastro")
                    .permitAll()
                    .requestMatchers("/usuario/me")
                    .authenticated()
                    .requestMatchers("derleta/me")
                    .authenticated()
                    .requestMatchers("/atualizar")
                    .authenticated()
                    .requestMatchers("/grupo-tematico/**")
                    .authenticated()
                    .requestMatchers("/chat/**")
                    .permitAll()
                    .requestMatchers("/arquivo/**")
                    .permitAll()
                    .anyRequest()
                    .permitAll())
        .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration corsConfiguration = new CorsConfiguration();
    corsConfiguration.setAllowedOrigins(
        Arrays.asList(
            "http://localhost:8082",
            "http://localhost:8081",
            "http://localhost:5173",
            "http://10.187.133.135:8081",
            "http://192.168.1.100:8081",
            "http://192.168.137.194:8081",
            "http://192.168.1.11:8081",
            "http://192.168.1.11:8081"));
    corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
    corsConfiguration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    corsConfiguration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfiguration);
    return source;
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }
}
