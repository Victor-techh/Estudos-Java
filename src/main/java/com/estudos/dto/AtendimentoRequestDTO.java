package com.estudos.dto;

import com.estudos.modelo.Estabelecimento;
import java.util.ArrayList;
import java.util.List;

public class AtendimentoRequestDTO {
    private Long usuarioId; // ATRIBUTO ADICIONADO PARA O H2
    private String uid;     // UID do Firebase
    private Estabelecimento estabelecimento;
    private String mensagem;
    private List<MensagemDTO> historico = new ArrayList<>();
    private int quantidadeMusicos = 1;

    public AtendimentoRequestDTO() {}

    public AtendimentoRequestDTO(Long usuarioId, Estabelecimento estabelecimento, String mensagem, List<MensagemDTO> historico, int quantidadeMusicos) {
        this.usuarioId = usuarioId;
        this.estabelecimento = estabelecimento;
        this.mensagem = mensagem;
        this.historico = historico != null ? historico : new ArrayList<>();
        this.quantidadeMusicos = quantidadeMusicos > 0 ? quantidadeMusicos : 1;
    }

    public Long getUsuarioId() { return usuarioId; }

    public void setUsuarioId(Object id) {
        if (id == null) {
            this.usuarioId = null;
            return;
        }
        if (id instanceof Number) {
            this.usuarioId = ((Number) id).longValue();
        } else {
            String str = id.toString().trim();
            try {
                this.usuarioId = Long.parseLong(str);
            } catch (NumberFormatException e) {
                this.uid = str;
            }
        }
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public Estabelecimento getEstabelecimento() { return estabelecimento; }
    public void setEstabelecimento(Estabelecimento estabelecimento) { this.estabelecimento = estabelecimento; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public List<MensagemDTO> getHistorico() { return historico; }
    public void setHistorico(List<MensagemDTO> historico) { this.historico = historico; }

    public int getQuantidadeMusicos() { return quantidadeMusicos; }
    public void setQuantidadeMusicos(int quantidadeMusicos) { this.quantidadeMusicos = quantidadeMusicos > 0 ? quantidadeMusicos : 1; }

    public int getQtdMusicos() { return this.quantidadeMusicos; }
    public void setQtdMusicos(int qtdMusicos) { setQuantidadeMusicos(qtdMusicos); }
}