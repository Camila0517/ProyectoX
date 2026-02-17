package com.giru.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository,
            BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<UsuarioDTO> getUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(usuario -> new UsuarioDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getRol()
        ))
                .toList();
    }

    @PostMapping
    public Usuario crearUsuario(@RequestBody Usuario usuario) {

        //Encriptar password antes de guardar
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        return usuarioRepository.save(usuario);
    }

    @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {

    var usuarioOptional = usuarioRepository.findByEmail(request.getEmail());

    if (usuarioOptional.isEmpty()) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Usuario no encontrado");
    }

    Usuario usuario = usuarioOptional.get();

    if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Contraseña incorrecta");
    }

    UsuarioDTO usuarioDTO = new UsuarioDTO(
            usuario.getId(),
            usuario.getUsername(),
            usuario.getEmail(),
            usuario.getRol()
    );

    return ResponseEntity.ok(usuarioDTO);
}


}
