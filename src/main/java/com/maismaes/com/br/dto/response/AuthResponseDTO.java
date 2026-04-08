package com.maismaes.com.br.dto.response;

import java.util.List;

public record AuthResponseDTO(
        String token,
        String role
) {
}
