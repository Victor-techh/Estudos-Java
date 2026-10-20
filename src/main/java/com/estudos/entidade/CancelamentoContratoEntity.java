package com.estudos.entidade;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_cancelamentos_contrato")
public class CancelamentoContratoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long contratoId;
    private Long canceladoPorUsuarioId;
    private String perfilCancelador;
    private LocalDateTime criadoEm;
    private boolean dentroJanela48Horas;
    private boolean atestadoSolicitado;
    private String statusRevisao;
    private Long revisadoPorUsuarioId;
    private LocalDateTime revisadoEm;
    private Double multaContratante;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getContratoId() { return contratoId; }
    public void setContratoId(Long contratoId) { this.contratoId = contratoId; }
    public Long getCanceladoPorUsuarioId() { return canceladoPorUsuarioId; }
    public void setCanceladoPorUsuarioId(Long canceladoPorUsuarioId) { this.canceladoPorUsuarioId = canceladoPorUsuarioId; }
    public String getPerfilCancelador() { return perfilCancelador; }
    public void setPerfilCancelador(String perfilCancelador) { this.perfilCancelador = perfilCancelador; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public boolean isDentroJanela48Horas() { return dentroJanela48Horas; }
    public void setDentroJanela48Horas(boolean dentroJanela48Horas) { this.dentroJanela48Horas = dentroJanela48Horas; }
    public boolean isAtestadoSolicitado() { return atestadoSolicitado; }
    public void setAtestadoSolicitado(boolean atestadoSolicitado) { this.atestadoSolicitado = atestadoSolicitado; }
    public String getStatusRevisao() { return statusRevisao; }
    public void setStatusRevisao(String statusRevisao) { this.statusRevisao = statusRevisao; }
    public Long getRevisadoPorUsuarioId() { return revisadoPorUsuarioId; }
    public void setRevisadoPorUsuarioId(Long revisadoPorUsuarioId) { this.revisadoPorUsuarioId = revisadoPorUsuarioId; }
    public LocalDateTime getRevisadoEm() { return revisadoEm; }
    public void setRevisadoEm(LocalDateTime revisadoEm) { this.revisadoEm = revisadoEm; }
    public Double getMultaContratante() { return multaContratante; }
    public void setMultaContratante(Double multaContratante) { this.multaContratante = multaContratante; }
}
