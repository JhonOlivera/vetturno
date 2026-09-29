package com.huellitas.vetturno.service;

import com.huellitas.vetturno.dto.VeterinarioDTO;
import com.huellitas.vetturno.dto.VeterinarioRequest;
import com.huellitas.vetturno.model.Veterinario;
import com.huellitas.vetturno.repository.VeterinarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VeterinarioService {

    private final VeterinarioRepository veterinarioRepository;

    public VeterinarioService(VeterinarioRepository veterinarioRepository) {
        this.veterinarioRepository = veterinarioRepository;
    }

    public VeterinarioDTO crear(VeterinarioRequest request) {
        Veterinario v = new Veterinario();
        v.setNombre(request.nombre());
        v.setEspecialidad(request.especialidad());
        return aDTO(veterinarioRepository.save(v));
    }

    public List<VeterinarioDTO> listar() {
        return veterinarioRepository.findAll().stream().map(this::aDTO).toList();
    }

    private VeterinarioDTO aDTO(Veterinario v) {
        return new VeterinarioDTO(v.getId(), v.getNombre(), v.getEspecialidad());
    }
}