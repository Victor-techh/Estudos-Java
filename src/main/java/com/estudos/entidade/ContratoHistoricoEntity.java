package com.estudos.entidade;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_contratos_historico")
public class ContratoHistoricoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String emailDestinatario;
    private String nomeEstabelecimento;
    
    @Column(columnDefinition = "TEXT")
    private String conteudoContrato;
    
    private String caminhoArquivoPdf;
    private LocalDateTime dataCriacao;

    public ContratoHistoricoEntity() {
        this.dataCriacao = LocalDateTime.now();
    }

    public ContratoHistoricoEntity(String emailDestinatario, String nomeEstabelecimento, String conteudoContrato, String caminhoArquivoPdf) {
        this.emailDestinatario = emailDestinatario;
        this.nomeEstabelecimento = nomeEstabelecimento;
        this.conteudoContrato = conteudoContrato;
        this.caminhoArquivoPdf = caminhoArquivoPdf;
        this.dataCriacao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getEmailDestinatario() { return emailDestinatario; }
    public String getNomeEstabelecimento() { return nomeEstabelecimento; }
    public String getConteudoContrato() { return conteudoContrato; }
    public String getCaminhoArquivoPdf() { return caminhoArquivoPdf; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
}