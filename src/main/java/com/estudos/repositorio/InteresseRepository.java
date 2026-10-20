package com.estudos.repositorio;

import com.estudos.entidade.InteresseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InteresseRepository extends JpaRepository<InteresseEntity, Long> {
    Optional<InteresseEntity> findByArtistaIdAndEstabelecimentoId(Long artistaId, Long estabelecimentoId);
    List<InteresseEntity> findByArtistaIdOrEstabelecimentoId(Long artistaId, Long estabelecimentoId);
    List<InteresseEntity> findByAtualizadoEmBefore(LocalDateTime atualizadoEm);
}
