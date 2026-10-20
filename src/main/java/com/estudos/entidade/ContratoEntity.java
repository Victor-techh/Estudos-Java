package com.estudos.entidade;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Lob;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_contratos")
public class ContratoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contratante;
    private String contratado;
    private Double valor;
    private Long artistaId;
    private Long estabelecimentoId;
    private Long interesseId;
    private String status;
    private String formacao;
    private Integer quantidadeMusicos;
    private Integer duracaoHoras;
    private LocalDateTime dataHoraApresentacao;
    private LocalDate dataPagamento;
    private String cpfCnpjContratante;
    private String nomeLegalContratante;
    private String enderecoContratante;
    private String emailContratante;
    private String cpfCnpjContratado;
    private String nomeLegalContratado;
    private String enderecoContratado;
    private String emailContratado;
    private Double descontoCancelamento;
    private Double valorLiquidoArtista;
    private Double multaCancelamentoContratante;
    private String statusCancelamento;
    private Double valorSom;
    private String condicoesSom;
    private Boolean estabelecimentoPossuiSom;
    private LocalDateTime aceiteContratanteEm;
    private LocalDateTime aceiteArtistaEm;
    private String hashMinuta;
    @Lob
    private byte[] pdf;

    // Construtor padrão obrigatório para o JPA/Jackson
    public ContratoEntity() {}

    public ContratoEntity(String contratante, String contratado, Double valor) {
        this.contratante = contratante;
        this.contratado = contratado;
        this.valor = valor;
    }

    // Getters e Setters obrigatórios para serialização JSON
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContratante() { return contratante; }
    public void setContratante(String contratante) { this.contratante = contratante; }

    public String getContratado() { return contratado; }
    public void setContratado(String contratado) { this.contratado = contratado; }

    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }

    public Long getArtistaId() { return artistaId; }
    public void setArtistaId(Long artistaId) { this.artistaId = artistaId; }
    public Long getEstabelecimentoId() { return estabelecimentoId; }
    public void setEstabelecimentoId(Long estabelecimentoId) { this.estabelecimentoId = estabelecimentoId; }
    public Long getInteresseId() { return interesseId; }
    public void setInteresseId(Long interesseId) { this.interesseId = interesseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFormacao() { return formacao; }
    public void setFormacao(String formacao) { this.formacao = formacao; }
    public Integer getQuantidadeMusicos() { return quantidadeMusicos; }
    public void setQuantidadeMusicos(Integer quantidadeMusicos) { this.quantidadeMusicos = quantidadeMusicos; }
    public Integer getDuracaoHoras() { return duracaoHoras; }
    public void setDuracaoHoras(Integer duracaoHoras) { this.duracaoHoras = duracaoHoras; }
    public LocalDateTime getDataHoraApresentacao() { return dataHoraApresentacao; }
    public void setDataHoraApresentacao(LocalDateTime dataHoraApresentacao) { this.dataHoraApresentacao = dataHoraApresentacao; }
    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }
    public String getCpfCnpjContratante() { return cpfCnpjContratante; }
    public void setCpfCnpjContratante(String cpfCnpjContratante) { this.cpfCnpjContratante = cpfCnpjContratante; }
    public String getNomeLegalContratante() { return nomeLegalContratante; }
    public void setNomeLegalContratante(String nomeLegalContratante) { this.nomeLegalContratante = nomeLegalContratante; }
    public String getEnderecoContratante() { return enderecoContratante; }
    public void setEnderecoContratante(String enderecoContratante) { this.enderecoContratante = enderecoContratante; }
    public String getEmailContratante() { return emailContratante; }
    public void setEmailContratante(String emailContratante) { this.emailContratante = emailContratante; }
    public String getCpfCnpjContratado() { return cpfCnpjContratado; }
    public void setCpfCnpjContratado(String cpfCnpjContratado) { this.cpfCnpjContratado = cpfCnpjContratado; }
    public String getNomeLegalContratado() { return nomeLegalContratado; }
    public void setNomeLegalContratado(String nomeLegalContratado) { this.nomeLegalContratado = nomeLegalContratado; }
    public String getEnderecoContratado() { return enderecoContratado; }
    public void setEnderecoContratado(String enderecoContratado) { this.enderecoContratado = enderecoContratado; }
    public String getEmailContratado() { return emailContratado; }
    public void setEmailContratado(String emailContratado) { this.emailContratado = emailContratado; }
    public Double getDescontoCancelamento() { return descontoCancelamento; }
    public void setDescontoCancelamento(Double descontoCancelamento) { this.descontoCancelamento = descontoCancelamento; }
    public Double getValorLiquidoArtista() { return valorLiquidoArtista; }
    public void setValorLiquidoArtista(Double valorLiquidoArtista) { this.valorLiquidoArtista = valorLiquidoArtista; }
    public Double getMultaCancelamentoContratante() { return multaCancelamentoContratante; }
    public void setMultaCancelamentoContratante(Double multaCancelamentoContratante) { this.multaCancelamentoContratante = multaCancelamentoContratante; }
    public String getStatusCancelamento() { return statusCancelamento; }
    public void setStatusCancelamento(String statusCancelamento) { this.statusCancelamento = statusCancelamento; }
    public Double getValorSom() { return valorSom; }
    public void setValorSom(Double valorSom) { this.valorSom = valorSom; }
    public String getCondicoesSom() { return condicoesSom; }
    public void setCondicoesSom(String condicoesSom) { this.condicoesSom = condicoesSom; }
    public Boolean getEstabelecimentoPossuiSom() { return estabelecimentoPossuiSom; }
    public void setEstabelecimentoPossuiSom(Boolean estabelecimentoPossuiSom) { this.estabelecimentoPossuiSom = estabelecimentoPossuiSom; }
    public LocalDateTime getAceiteContratanteEm() { return aceiteContratanteEm; }
    public void setAceiteContratanteEm(LocalDateTime aceiteContratanteEm) { this.aceiteContratanteEm = aceiteContratanteEm; }
    public LocalDateTime getAceiteArtistaEm() { return aceiteArtistaEm; }
    public void setAceiteArtistaEm(LocalDateTime aceiteArtistaEm) { this.aceiteArtistaEm = aceiteArtistaEm; }
    public String getHashMinuta() { return hashMinuta; }
    public void setHashMinuta(String hashMinuta) { this.hashMinuta = hashMinuta; }
    public byte[] getPdf() { return pdf; }
    public void setPdf(byte[] pdf) { this.pdf = pdf; }
}