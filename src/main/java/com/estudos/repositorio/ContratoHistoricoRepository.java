package com.estudos.repositorio;

import com.estudos.entidade.ContratoHistoricoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContratoHistoricoRepository extends JpaRepository<ContratoHistoricoEntity, Long> {
    List<ContratoHistoricoEntity> findByUsuarioId(Long usuarioId);
}