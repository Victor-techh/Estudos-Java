package com.estudos.entidade;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_contratos")
public class ContratoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String estabelecimentoNome;
    private String artistaNome;
    private double valorFechado;
    private LocalDateTime dataRegistro;

    public ContratoEntity() {}

    public ContratoEntity(String estabelecimentoNome, String artistaNome, double valorFechado) {
        this.estabelecimentoNome = estabelecimentoNome;
        this.artistaNome = artistaNome;
        this.valorFechado = valorFechado;
        this.dataRegistro = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getEstabelecimentoNome() { return estabelecimentoNome; }
    public String getArtistaNome() { return artistaNome; }
    public double getValorFechado() { return valorFechado; }
    public LocalDateTime getDataRegistro() { return dataRegistro; }
}