package com.huellitas.vetturno.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CitaRequest(
        @NotNull(message = "La fecha y hora son obligatorias")
        @Future(message = "La fecha debe ser futura")
        LocalDateTime fechaHora,

        @NotBlank(message = "El motivo es obligatorio")
        String motivo,

        @NotNull(message = "La mascota es obligatoria")
        Long mascotaId,

        @NotNull(message = "El veterinario es obligatorio")
        Long veterinarioId) {}