package com.estudos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ContratoRequest(
        Long interesseId,
        LocalDateTime dataHoraApresentacao,
        Integer duracaoHoras,
        Integer quantidadeMusicos,
        BigDecimal cacheBruto,
        LocalDate dataPagamento,
        BigDecimal valorSom,
        String condicoesSom
) {
}
