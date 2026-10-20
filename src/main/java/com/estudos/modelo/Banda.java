package com.estudos.modelo;

public class Banda extends Artista {
    private double taxaLogistica;

    public Banda(String nome, String estiloMusical, double valorShow, double taxaLogistica) {
        super(nome, estiloMusical, valorShow);
        this.taxaLogistica = taxaLogistica;
    }

    @Override
    public double getValorShow() {
        return super.getValorShow() + this.taxaLogistica;
    }

    @Override
    public double getPisoLegal() {
        return 1500.0;
    }
}