package com.huellitas.vetturno.service;

import com.huellitas.vetturno.dto.PropietarioDTO;
import com.huellitas.vetturno.dto.PropietarioRequest;
import com.huellitas.vetturno.model.Propietario;
import com.huellitas.vetturno.repository.PropietarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropietarioService {

    private final PropietarioRepository propietarioRepository;

    public PropietarioService(PropietarioRepository propietarioRepository) {
        this.propietarioRepository = propietarioRepository;
    }

    public PropietarioDTO crear(PropietarioRequest request) {
        Propietario p = new Propietario();
        p.setNombre(request.nombre());
        p.setTelefono(request.telefono());
        p.setEmail(request.email());
        return aDTO(propietarioRepository.save(p));
    }

    public List<PropietarioDTO> listar() {
        return propietarioRepository.findAll().stream().map(this::aDTO).toList();
    }

    private PropietarioDTO aDTO(Propietario p) {
        return new PropietarioDTO(p.getId(), p.getNombre(), p.getTelefono(), p.getEmail());
    }
}