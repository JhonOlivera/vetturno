package com.huellitas.vetturno.service;

import com.huellitas.vetturno.dto.AuthResponse;
import com.huellitas.vetturno.dto.LoginRequest;
import com.huellitas.vetturno.dto.RegistroRequest;
import com.huellitas.vetturno.exception.ReglaNegocioException;
import com.huellitas.vetturno.model.Rol;
import com.huellitas.vetturno.model.Usuario;
import com.huellitas.vetturno.repository.UsuarioRepository;
import com.huellitas.vetturno.security.JwtService;
import com.huellitas.vetturno.security.UsuarioDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UsuarioDetailsService usuarioDetailsService;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       UsuarioDetailsService usuarioDetailsService,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.usuarioDetailsService = usuarioDetailsService;
        this.jwtService = jwtService;
    }

    public AuthResponse registrar(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ReglaNegocioException("El email ya está registrado");
        }
        Usuario u = new Usuario();
        u.setEmail(request.email());
        u.setPassword(passwordEncoder.encode(request.password()));
        u.setRol(Rol.USER);
        usuarioRepository.save(u);

        UserDetails detalles = usuarioDetailsService.loadUserByUsername(u.getEmail());
        return new AuthResponse(jwtService.generarToken(detalles));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserDetails detalles = usuarioDetailsService.loadUserByUsername(request.email());
        return new AuthResponse(jwtService.generarToken(detalles));
    }
}