package com.estudos.repositorio;

import com.estudos.entidade.ContratoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContratoRepository extends JpaRepository<ContratoEntity, Long> {
    List<ContratoEntity> findByArtistaIdOrEstabelecimentoId(Long artistaId, Long estabelecimentoId);
    List<ContratoEntity> findByArtistaIdAndStatusOrderByIdAsc(Long artistaId, String status);
}