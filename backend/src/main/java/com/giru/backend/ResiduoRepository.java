package com.giru.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResiduoRepository extends JpaRepository<Residuo, Long> {

    List<Residuo> findByUsuarioId(Long usuarioId);
    List<Residuo> findByEstado(EstadoResiduo estado);
    

}
