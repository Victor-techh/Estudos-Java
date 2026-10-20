package com.estudos.repositorio;

import com.estudos.entidade.CancelamentoContratoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface CancelamentoContratoRepository extends JpaRepository<CancelamentoContratoEntity, Long> {
    Optional<CancelamentoContratoEntity> findByContratoId(Long contratoId);
    List<CancelamentoContratoEntity> findByStatusRevisao(String statusRevisao);
}
