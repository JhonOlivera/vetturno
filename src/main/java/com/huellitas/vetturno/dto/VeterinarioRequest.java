package com.huellitas.vetturno.dto;

import jakarta.validation.constraints.NotBlank;

public record VeterinarioRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "La especialidad es obligatoria") String especialidad) {}