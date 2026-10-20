package com.estudos.entidade;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_penalidades_cancelamento")
public class PenalidadeCancelamentoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long artistaId;
    private Long contratoCanceladoId;
    private Long cancelamentoId;
    private Long contratoCompensacaoId;
    private Double valor;
    private LocalDateTime criadaEm;
    private LocalDateTime pontoNegativoExpiraEm;
    private boolean aplicada;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getArtistaId() { return artistaId; }
    public void setArtistaId(Long artistaId) { this.artistaId = artistaId; }
    public Long getContratoCanceladoId() { return contratoCanceladoId; }
    public void setContratoCanceladoId(Long contratoCanceladoId) { this.contratoCanceladoId = contratoCanceladoId; }
    public Long getCancelamentoId() { return cancelamentoId; }
    public void setCancelamentoId(Long cancelamentoId) { this.cancelamentoId = cancelamentoId; }
    public Long getContratoCompensacaoId() { return contratoCompensacaoId; }
    public void setContratoCompensacaoId(Long contratoCompensacaoId) { this.contratoCompensacaoId = contratoCompensacaoId; }
    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public void setCriadaEm(LocalDateTime criadaEm) { this.criadaEm = criadaEm; }
    public LocalDateTime getPontoNegativoExpiraEm() { return pontoNegativoExpiraEm; }
    public void setPontoNegativoExpiraEm(LocalDateTime pontoNegativoExpiraEm) { this.pontoNegativoExpiraEm = pontoNegativoExpiraEm; }
    public boolean isAplicada() { return aplicada; }
    public void setAplicada(boolean aplicada) { this.aplicada = aplicada; }
}
