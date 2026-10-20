package com.estudos.repositorio;

import com.estudos.entidade.PenalidadeCancelamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PenalidadeCancelamentoRepository extends JpaRepository<PenalidadeCancelamentoEntity, Long> {
    Optional<PenalidadeCancelamentoEntity> findFirstByArtistaIdAndAplicadaFalseOrderByCriadaEmAsc(Long artistaId);
    List<PenalidadeCancelamentoEntity> findByArtistaIdAndPontoNegativoExpiraEmAfter(Long artistaId, LocalDateTime now);
}
