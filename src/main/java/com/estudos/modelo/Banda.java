package com.estudos.modelo;

public class Banda extends Artista {
    private double taxaLogistica;
    private final int quantidadeMusicos;

    public Banda(String nome, String estiloMusical, double valorShow, double taxaLogistica) {
        this(nome, estiloMusical, valorShow, taxaLogistica, Formacao.TRIO.getQuantidadeMusicos());
    }

    public Banda(String nome, String estiloMusical, double valorShow, double taxaLogistica, int quantidadeMusicos) {
        super(nome, estiloMusical, valorShow);
        if (Formacao.fromQuantidadeMusicos(quantidadeMusicos) == null || quantidadeMusicos == 1) {
            throw new IllegalArgumentException("Banda deve ter formação Duo ou Trio.");
        }
        this.taxaLogistica = taxaLogistica;
        this.quantidadeMusicos = quantidadeMusicos;
    }

    @Override
    public double getValorShow() {
        return super.getValorShow() + this.taxaLogistica;
    }

    @Override
    public double getCacheMinimo() {
        return Formacao.fromQuantidadeMusicos(quantidadeMusicos).getCacheMinimo();
    }

    public int getQuantidadeMusicos() {
        return quantidadeMusicos;
    }
}