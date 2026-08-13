package com.estudos.modelo;

public class Proposta {
    private Artista artista;
    private Estabelecimento estabelecimento;
    private boolean aprovada;
    private String motivoRecusa;

    public Proposta(Artista artista, Estabelecimento estabelecimento) {
        this.artista = artista;
        this.estabelecimento = estabelecimento;

        boolean estiloValido = artista.getEstiloMusical().equalsIgnoreCase(estabelecimento.getEstiloDesejado());
        boolean valorValido = artista.getValorShow() <= estabelecimento.getOrcamentoMaximo();

        if (estiloValido && valorValido) {
            this.aprovada = true;
            this.motivoRecusa = "Nenhum (Proposta Aprovada)";
        } else {
            this.aprovada = false;
            if (!estiloValido && !valorValido) {
                this.motivoRecusa = "Estilo incompatível e valor excede o orçamento";
            } else if (!estiloValido) {
                this.motivoRecusa = "Estilo musical incompatível (Desejado: " + estabelecimento.getEstiloDesejado() + 
                                   " | Artista: " + artista.getEstiloMusical() + ")";
            } else {
                this.motivoRecusa = "Valor do show (R$ " + artista.getValorShow() + 
                                   ") excede o orçamento máximo (R$ " + estabelecimento.getOrcamentoMaximo() + ")";
            }
        }
    }

    public Artista getArtista() { return this.artista; }
    public Estabelecimento getEstabelecimento() { return this.estabelecimento; }
    public boolean isAprovada() { return this.aprovada; }
    public String getMotivoRecusa() { return this.motivoRecusa; }
}