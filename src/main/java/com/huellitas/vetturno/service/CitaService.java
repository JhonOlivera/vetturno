package com.huellitas.vetturno.service;

import com.huellitas.vetturno.dto.CitaDTO;
import com.huellitas.vetturno.dto.CitaRequest;
import com.huellitas.vetturno.exception.ReglaNegocioException;
import com.huellitas.vetturno.model.Cita;
import com.huellitas.vetturno.model.Mascota;
import com.huellitas.vetturno.model.Veterinario;
import com.huellitas.vetturno.repository.CitaRepository;
import com.huellitas.vetturno.repository.MascotaRepository;
import com.huellitas.vetturno.repository.VeterinarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CitaService {

    private final CitaRepository citaRepository;
    private final MascotaRepository mascotaRepository;
    private final VeterinarioRepository veterinarioRepository;

    public CitaService(CitaRepository citaRepository,
                       MascotaRepository mascotaRepository,
                       VeterinarioRepository veterinarioRepository) {
        this.citaRepository = citaRepository;
        this.mascotaRepository = mascotaRepository;
        this.veterinarioRepository = veterinarioRepository;
    }

    public CitaDTO agendar(CitaRequest request) {
        Mascota mascota = mascotaRepository.findById(request.mascotaId())
                .orElseThrow(() -> new ReglaNegocioException(
                        "La mascota con id " + request.mascotaId() + " no existe"));
        Veterinario veterinario = veterinarioRepository.findById(request.veterinarioId())
                .orElseThrow(() -> new ReglaNegocioException(
                        "El veterinario con id " + request.veterinarioId() + " no existe"));

        if (!request.fechaHora().isAfter(LocalDateTime.now())) {
            throw new ReglaNegocioException("La fecha de la cita debe ser futura");
        }
        if (citaRepository.existsByVeterinarioIdAndFechaHora(
                veterinario.getId(), request.fechaHora())) {
            throw new ReglaNegocioException("El veterinario ya tiene una cita en ese horario");
        }

        Cita cita = new Cita();
        cita.setFechaHora(request.fechaHora());
        cita.setMotivo(request.motivo());
        cita.setMascota(mascota);
        cita.setVeterinario(veterinario);
        return aDTO(citaRepository.save(cita));
    }

    public List<CitaDTO> listar() {
        return citaRepository.findAll().stream().map(this::aDTO).toList();
    }

    public List<CitaDTO> listarPorVeterinario(Long veterinarioId) {
        return citaRepository.findByVeterinarioId(veterinarioId).stream()
                .map(this::aDTO).toList();
    }

    private CitaDTO aDTO(Cita c) {
        return new CitaDTO(c.getId(), c.getFechaHora(), c.getMotivo(),
                c.getMascota().getNombre(),
                c.getMascota().getPropietario().getNombre(),
                c.getVeterinario().getNombre());
    }
}