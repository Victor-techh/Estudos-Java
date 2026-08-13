package com.estudos.modelo;

public abstract class Artista {
    private String nome;
    private String estiloMusical;
    private double valorShow;

    public Artista(String nome, String estiloMusical, double valorShow) {
        this.nome = nome;
        this.estiloMusical = estiloMusical;
        this.valorShow = valorShow;
    }

    public void setValorShow(double valorShow) {
        if (valorShow >= 0) {
            this.valorShow = valorShow;
        } else {
            System.out.println("Erro: O valor do show não pode ser negativo.");
        }
    }

    public String getNome() { return nome; }
    public String getEstiloMusical() { return estiloMusical; }
    public double getValorShow() { return valorShow; }

    public abstract double getPisoLegal();
}