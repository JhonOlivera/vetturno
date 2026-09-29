package com.huellitas.vetturno.service;

import com.huellitas.vetturno.dto.MascotaDTO;
import com.huellitas.vetturno.dto.MascotaRequest;
import com.huellitas.vetturno.exception.ReglaNegocioException;
import com.huellitas.vetturno.model.Mascota;
import com.huellitas.vetturno.model.Propietario;
import com.huellitas.vetturno.repository.MascotaRepository;
import com.huellitas.vetturno.repository.PropietarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MascotaService {

    private final MascotaRepository mascotaRepository;
    private final PropietarioRepository propietarioRepository;

    public MascotaService(MascotaRepository mascotaRepository,
                          PropietarioRepository propietarioRepository) {
        this.mascotaRepository = mascotaRepository;
        this.propietarioRepository = propietarioRepository;
    }

    public MascotaDTO crear(MascotaRequest request) {
        Propietario propietario = propietarioRepository.findById(request.propietarioId())
                .orElseThrow(() -> new ReglaNegocioException(
                        "El propietario con id " + request.propietarioId() + " no existe"));

        Mascota m = new Mascota();
        m.setNombre(request.nombre());
        m.setEspecie(request.especie());
        m.setRaza(request.raza());
        m.setPropietario(propietario);
        return aDTO(mascotaRepository.save(m));
    }

    public List<MascotaDTO> listar() {
        return mascotaRepository.findAll().stream().map(this::aDTO).toList();
    }

    private MascotaDTO aDTO(Mascota m) {
        return new MascotaDTO(m.getId(), m.getNombre(), m.getEspecie(), m.getRaza(),
                m.getPropietario().getId(), m.getPropietario().getNombre());
    }
}