package com.estudos.modelo;

public class Estabelecimento {
    private String nome;
    private String tipo;
    private String endereco;
    private double orcamentoMaximo;
    private String estiloMusicalDesejado;
    private boolean possuiEquipamentoSom;

    public Estabelecimento(String nome, String tipo, String endereco, double orcamentoMaximo, String estiloMusicalDesejado, boolean possuiEquipamentoSom) {
        this.nome = nome;
        this.tipo = tipo;
        this.endereco = endereco;
        this.orcamentoMaximo = orcamentoMaximo;
        this.estiloMusicalDesejado = estiloMusicalDesejado;
        this.possuiEquipamentoSom = possuiEquipamentoSom;
    }

    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
    public String getEndereco() { return endereco; }
    public double getOrcamentoMaximo() { return orcamentoMaximo; }
    public boolean isPossuiEquipamentoSom() { return possuiEquipamentoSom; }
    // Retorna o estilo desejado exigido pela classe Proposta
    public String getEstiloDesejado() {
        return this.estiloMusicalDesejado; // Ajuste para o nome do seu atributo interno se for diferente
    }

    // Mantém compatibilidade com a camada de serviço
    public String getEstiloMusicalDesejado() {
        return getEstiloDesejado();
    }
}