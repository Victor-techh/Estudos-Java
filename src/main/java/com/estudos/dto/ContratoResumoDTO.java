package com.estudos.dto;

import com.estudos.entidade.ContratoEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ContratoResumoDTO(
        Long id,
        String status,
        String contratante,
        String contratado,
        Double cacheBruto,
        Double descontoCancelamento,
        Double valorLiquidoArtista,
        LocalDateTime dataHoraApresentacao,
        LocalDate dataPagamento,
        Integer quantidadeMusicos,
        Integer duracaoHoras,
        String urlPdf
) {
    public static ContratoResumoDTO from(ContratoEntity contrato) {
        return new ContratoResumoDTO(
                contrato.getId(),
                contrato.getStatus(),
                contrato.getContratante(),
                contrato.getContratado(),
                contrato.getValor(),
                contrato.getDescontoCancelamento(),
                contrato.getValorLiquidoArtista(),
                contrato.getDataHoraApresentacao(),
                contrato.getDataPagamento(),
                contrato.getQuantidadeMusicos(),
                contrato.getDuracaoHoras(),
                "/api/contratos/" + contrato.getId() + "/pdf"
        );
    }
}
