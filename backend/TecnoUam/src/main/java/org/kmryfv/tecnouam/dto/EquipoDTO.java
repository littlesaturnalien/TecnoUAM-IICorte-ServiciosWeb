package org.kmryfv.tecnouam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record EquipoDTO(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long id,
        @NotBlank String codigo,
        @NotBlank String nombre,
        @NotBlank String tipo,
        @NotBlank String estado
) {
}
