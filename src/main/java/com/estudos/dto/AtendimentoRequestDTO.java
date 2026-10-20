package com.estudos.dto;

import com.estudos.modelo.Estabelecimento;
import java.util.ArrayList;
import java.util.List;

public class AtendimentoRequestDTO {
    private Long usuarioId; // ATRIBUTO ADICIONADO PARA O H2
    private Estabelecimento estabelecimento;
    private String mensagem;
    private List<MensagemDTO> historico = new ArrayList<>();
    private int quantidadeMusicos = 1;
    private Long interesseId;

    public AtendimentoRequestDTO() {}

    public AtendimentoRequestDTO(Long usuarioId, Estabelecimento estabelecimento, String mensagem, List<MensagemDTO> historico, int quantidadeMusicos) {
        this.usuarioId = usuarioId;
        this.estabelecimento = estabelecimento;
        this.mensagem = mensagem;
        this.historico = historico != null ? historico : new ArrayList<>();
        this.quantidadeMusicos = quantidadeMusicos > 0 ? quantidadeMusicos : 1;
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Estabelecimento getEstabelecimento() { return estabelecimento; }
    public void setEstabelecimento(Estabelecimento estabelecimento) { this.estabelecimento = estabelecimento; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public List<MensagemDTO> getHistorico() { return historico; }
    public void setHistorico(List<MensagemDTO> historico) { this.historico = historico; }

    public int getQuantidadeMusicos() { return quantidadeMusicos; }
    public void setQuantidadeMusicos(int quantidadeMusicos) { this.quantidadeMusicos = quantidadeMusicos; }

    public Long getInteresseId() { return interesseId; }
    public void setInteresseId(Long interesseId) { this.interesseId = interesseId; }
}