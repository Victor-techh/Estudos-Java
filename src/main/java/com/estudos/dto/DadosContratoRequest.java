package com.estudos.dto;

public record DadosContratoRequest(
        String nomeLegal,
        String cpfCnpj,
        String endereco
) {
}
