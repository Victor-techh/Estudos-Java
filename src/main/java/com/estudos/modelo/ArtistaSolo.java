package com.estudos.modelo;

public class ArtistaSolo extends Artista {
    public ArtistaSolo(String nome, String estiloMusical, double valorShow) {
        super(nome, estiloMusical, valorShow);
    }

    @Override
    public double getPisoLegal() {
        return 500.0;
    }
}