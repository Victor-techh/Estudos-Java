package com.estudos.repositorio;

import com.estudos.entidade.ContratoHistoricoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContratoHistoricoRepository extends JpaRepository<ContratoHistoricoEntity, Long> {
}