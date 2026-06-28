package com.maismaes.com.br.utils;

import com.maismaes.com.br.dto.request.DenunciaGrupoFilterDTO;
import com.maismaes.com.br.entities.grupo_tematico.DenunciarGrupo;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class DenunciarGrupoSpecification {

    public static Specification<DenunciarGrupo> filtrar(DenunciaGrupoFilterDTO filtro) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro != null) {

                if (filtro.status() != null && !filtro.status().toString().isBlank()) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), filtro.status()));
                }


                if (filtro.grupoId() != null && filtro.grupoId() > 0) {
                    predicates.add(criteriaBuilder.equal(root.get("grupo").get("id"), filtro.grupoId()));
                }


                if (filtro.usuarioId() != null && !filtro.usuarioId().toString().isBlank()) {
                    predicates.add(criteriaBuilder.equal(root.get("usuario").get("id"), filtro.usuarioId()));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}