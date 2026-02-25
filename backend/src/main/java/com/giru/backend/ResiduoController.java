package com.giru.backend;

import com.giru.backend.EstadoResiduo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/residuos")
@CrossOrigin
public class ResiduoController {

    private final ResiduoRepository residuoRepository;
    private final UsuarioRepository usuarioRepository;

    public ResiduoController(ResiduoRepository residuoRepository,
            UsuarioRepository usuarioRepository) {
        this.residuoRepository = residuoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Obtener todos
    @GetMapping
    public List<Residuo> getResiduos() {
        return residuoRepository.findAll();
    }

    // Crear residuo
    @PostMapping("/{usuarioId}")
    public Residuo crearResiduo(@PathVariable Long usuarioId,
            @RequestBody Residuo residuo) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!usuario.getRol().equals(Rol.CIUDADANO)) {
            throw new RuntimeException("Solo los ciudadanos pueden crear residuos");
        }

        residuo.setUsuario(usuario);
        residuo.setEstado(EstadoResiduo.PENDIENTE);

        return residuoRepository.save(residuo);
    }

    // Obtener residuos por usuario
    @GetMapping("/usuario/{usuarioId}")
    public List<Residuo> getPorUsuario(@PathVariable Long usuarioId) {
        return residuoRepository.findByUsuarioId(usuarioId);
    }

    @GetMapping("/pendientes")
    public List<Residuo> getPendientes() {
        return residuoRepository.findByEstado(EstadoResiduo.PENDIENTE);
    }

    @PutMapping("/{id}/recoger/{usuarioId}")
    public Residuo recogerResiduo(@PathVariable Long id,
            @PathVariable Long usuarioId) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!usuario.getRol().equals(Rol.RECOLECTOR)) {
            throw new RuntimeException("Solo los recolectores pueden recoger residuos");
        }

        Residuo residuo = residuoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Residuo no encontrado"));

        residuo.setEstado(EstadoResiduo.RECOGIDO);

        return residuoRepository.save(residuo);
    }

}
