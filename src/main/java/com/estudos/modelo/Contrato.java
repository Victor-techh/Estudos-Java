package com.estudos.modelo;

import com.estudos.excecao.InfracaoLegalException;

public class Contrato {
    private Proposta proposta;

    public Contrato(Proposta proposta) throws InfracaoLegalException {
        if (!proposta.isAprovada()) {
            throw new IllegalArgumentException("Proposta recusada pelo estabelecimento.");
        }

        double cacheMinimo = proposta.getArtista().getCacheMinimo();

        if (proposta.getArtista().getCacheBruto() < cacheMinimo) {
            throw new InfracaoLegalException("Cachê de R$ " + proposta.getArtista().getCacheBruto() +
                    " é inferior ao mínimo de referência informado para a formação (R$ " + cacheMinimo + ")");
        }

        this.proposta = proposta;
    }

    public Proposta getProposta() { return proposta; }
}