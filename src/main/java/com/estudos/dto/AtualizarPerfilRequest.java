package com.estudos.dto;

public record AtualizarPerfilRequest(
        String nome,
        String nomeLegal,
        String tipoPerfil,
        String generoMusical,
        String estabelecimento,
        String endereco,
        String cidade,
        String descricao,
        Integer quantidadeMusicos,
        String generoDesejado,
        Boolean possuiEquipamentoSom,
        String tipoEstabelecimento,
        String cpfCnpj
) {
}
