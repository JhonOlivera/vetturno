package com.huellitas.vetturno.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MascotaRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "La especie es obligatoria") String especie,
        String raza,
        @NotNull(message = "El propietario es obligatorio") Long propietarioId) {}