package com.maismaes.com.br.controller;

import com.maismaes.com.br.dto.response.DelegaciaProximaResponseDTO;
import com.maismaes.com.br.service.DelegaciaImportService;
import com.maismaes.com.br.service.DelegaciaService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("delegacias")
public class DelegaciaController {

    private final DelegaciaService delegaciaService;
    private final DelegaciaImportService delegaciaImportService;

    @Operation(summary = "Lista delegacias próximas", description = "Recebe a localização (lat/lon) e devolve as delegacias dentro do raio informado, ordenadas da mais próxima para a mais distante.")
    @GetMapping("/proximas")
    public ResponseEntity<List<DelegaciaProximaResponseDTO>> proximas(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "5") double raioKm,
            @RequestParam(defaultValue = "10") int limite) {
        // Não logar lat/lon: é localização pessoal do usuário.
        return ResponseEntity.ok(delegaciaService.buscarProximas(lat, lon, raioKm, limite));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Importa delegacias do OpenStreetMap", description = "Busca as delegacias de um estado (ex.: BR-SP) na Overpass API e grava/atualiza na tabela. Apenas administrador.")
    @PostMapping("/importar")
    public ResponseEntity<Map<String, Object>> importar(@RequestParam String estado) {
        log.info("[REQUISIÇÃO] - Importação de delegacias do estado {}", estado);
        int total = delegaciaImportService.importar(estado);
        return ResponseEntity.ok(Map.of("estado", estado, "importadas", total));
    }
}