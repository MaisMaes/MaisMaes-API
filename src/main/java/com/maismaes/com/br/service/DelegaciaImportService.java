package com.maismaes.com.br.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.maismaes.com.br.entities.Delegacia;
import com.maismaes.com.br.repository.DelegaciaRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

/**
 * Importa delegacias do OpenStreetMap (Overpass API) e grava/atualiza na
 * tabela. Requer Spring Boot
 * 3.2+ (RestClient). Em versões anteriores, troque por RestTemplate.
 */
@Slf4j
@Service
public class DelegaciaImportService {

  private static final Pattern ESTADO = Pattern.compile("^BR-[A-Z]{2}$");

  private final RestClient restClient;
  private final DelegaciaRepository delegaciaRepository;

  public DelegaciaImportService(
      RestClient.Builder builder, DelegaciaRepository delegaciaRepository) {
    var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(10_000);
    factory.setReadTimeout(200_000); // estados grandes demoram

    this.restClient = builder
        .baseUrl("https://overpass-api.de")
        .requestFactory(factory)
        .defaultHeader(HttpHeaders.USER_AGENT, "maismaes-backend/1.0")
        .build();
    this.delegaciaRepository = delegaciaRepository;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  record OverpassResponse(List<Element> elements) {
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Element(
      String type, long id, Double lat, Double lon, Center center, Map<String, String> tags) {
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Center(double lat, double lon) {
  }

  /** @param estado código ISO do estado, ex.: BR-SP */
  public int importar(String estado) {
    if (estado == null || !ESTADO.matcher(estado).matches()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Use o formato BR-XX, ex.: BR-SP");
    }

    log.info("[Delegacias] Importação iniciada | Estado: {}", estado);

    String query = """
        [out:json][timeout:180];
        area["ISO3166-2"="%s"]->.a;
        (
          node["amenity"="police"](area.a);
          way["amenity"="police"](area.a);
        );
        out center tags;
        """
        .formatted(estado);

    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("data", query);

    OverpassResponse resposta = restClient
        .post()
        .uri("/api/interpreter")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(form)
        .retrieve()
        .body(OverpassResponse.class);

    if (resposta == null || resposta.elements() == null) {
      log.warn("[Delegacias] Overpass retornou vazio | Estado: {}", estado);
      return 0;
    }

    // Carrega os já existentes de uma vez, para atualizar em vez de duplicar
    Map<String, Delegacia> existentes = delegaciaRepository.findAll().stream()
        .filter(d -> d.getExternalId() != null)
        .collect(Collectors.toMap(Delegacia::getExternalId, Function.identity(), (a, b) -> a));

    List<Delegacia> paraSalvar = new ArrayList<>();
    for (Element el : resposta.elements()) {
      Double lat = el.lat() != null ? el.lat() : (el.center() != null ? el.center().lat() : null);
      Double lon = el.lon() != null ? el.lon() : (el.center() != null ? el.center().lon() : null);
      if (lat == null || lon == null)
        continue;

      String externalId = el.type() + "/" + el.id();
      Map<String, String> tags = el.tags() != null ? el.tags() : Map.of();

      Delegacia d = existentes.getOrDefault(externalId, new Delegacia());
      d.setExternalId(externalId);
      d.setNome(tags.getOrDefault("name", "Delegacia"));
      d.setEndereco(montarEndereco(tags));
      d.setTelefone(tags.getOrDefault("phone", tags.get("contact:phone")));
      d.setLatitude(lat);
      d.setLongitude(lon);
      paraSalvar.add(d);
    }

    delegaciaRepository.saveAll(paraSalvar);
    log.info("[Delegacias] Importação concluída | Estado: {} | Salvas: {}", estado, paraSalvar.size());
    return paraSalvar.size();
  }

  private String montarEndereco(Map<String, String> t) {
    return Stream.of(
        t.get("addr:street"), t.get("addr:housenumber"), t.get("addr:suburb"), t.get("addr:city"))
        .filter(Objects::nonNull)
        .collect(Collectors.joining(", "));
  }
}