package org.kmryfv.tecnouam.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;

import java.time.LocalDate;

public record PrestamoDTO(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Long id,
        @NotNull Long equipoId,
        @NotBlank String solicitante,
        @JsonFormat(pattern = "dd-MM-yyyy")
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        LocalDate fechaPrestamo,
        @JsonFormat(pattern = "dd-MM-yyyy")
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        LocalDate fechaDevolucion,
        @NotBlank String estadoPrestamo
) {
}
