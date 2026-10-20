package com.estudos.modelo;

import java.util.Arrays;

public enum Formacao {
    SOLO(1, "Solo"),
    DUO(2, "Duo"),
    TRIO(3, "Trio");

    public static final double CACHE_MINIMO_POR_MUSICO = 400.0;
    public static final int DURACAO_BASE_HORAS = 3;

    private final int quantidadeMusicos;
    private final String descricao;

    Formacao(int quantidadeMusicos, String descricao) {
        this.quantidadeMusicos = quantidadeMusicos;
        this.descricao = descricao;
    }

    public int getQuantidadeMusicos() {
        return quantidadeMusicos;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getCacheMinimo() {
        return quantidadeMusicos * CACHE_MINIMO_POR_MUSICO;
    }

    public static Formacao fromQuantidadeMusicos(int quantidadeMusicos) {
        return Arrays.stream(values())
                .filter(formacao -> formacao.quantidadeMusicos == quantidadeMusicos)
                .findFirst()
                .orElse(null);
    }
}
