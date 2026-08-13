package com.estudos.modelo;

import com.estudos.excecao.InfracaoLegalException;

public class Contrato {
    private Proposta proposta;

    public Contrato(Proposta proposta) throws InfracaoLegalException {
        if (!proposta.isAprovada()) {
            throw new IllegalArgumentException("Proposta recusada pelo estabelecimento.");
        }

        double pisoLegal = proposta.getArtista().getPisoLegal();

        if (proposta.getArtista().getValorShow() < pisoLegal) {
            throw new InfracaoLegalException("Cachê de R$ " + proposta.getArtista().getValorShow() + 
                                              " é inferior ao piso legal exigido para a categoria (R$ " + pisoLegal + ")");
        }

        this.proposta = proposta;
    }

    public Proposta getProposta() { return proposta; }
}