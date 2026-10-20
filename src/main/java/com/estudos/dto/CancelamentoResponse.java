package com.estudos.dto;

import com.estudos.entidade.CancelamentoContratoEntity;

import java.time.LocalDateTime;

public record CancelamentoResponse(
        Long id,
        Long contratoId,
        String perfilCancelador,
        LocalDateTime criadoEm,
        boolean dentroJanela48Horas,
        String statusRevisao,
        Double multaContratante
) {
    public static CancelamentoResponse from(CancelamentoContratoEntity cancelamento) {
        return new CancelamentoResponse(
                cancelamento.getId(),
                cancelamento.getContratoId(),
                cancelamento.getPerfilCancelador(),
                cancelamento.getCriadoEm(),
                cancelamento.isDentroJanela48Horas(),
                cancelamento.getStatusRevisao(),
                cancelamento.getMultaContratante()
        );
    }
}
