package com.estudos.controller;

import com.estudos.dto.MensagemDTO;
import com.estudos.modelo.Estabelecimento;
import com.estudos.servico.AgenteVendasService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST responsável por expor o endpoint de chat da negociação.
 */
@RestController
@RequestMapping("/api/agente")
public class AgenteController {

    private final AgenteVendasService agenteVendasService;

    public AgenteController(AgenteVendasService agenteVendasService) {
        this.agenteVendasService = agenteVendasService;
    }

    /**
     * Recebe as mensagens da interface web e repassa ao serviço comercial.
     */
    @PostMapping("/chat")
    public String conversar(@RequestBody SolicitacaoChat request) {
        // Correção: adição do argumento double (0.0) exigido pela classe Estabelecimento
        Estabelecimento est = new Estabelecimento(
                "Promenade Bar",
                "Bar / Restaurante",
                "Rua Central, 100",
                0.0,
                "Jazz / Lounge",
                true
        );

        return agenteVendasService.processarAtendimento(
                est,
                request.getMensagem(),
                request.getHistorico(),
                request.getQtdMusicos()
        );
    }

    public static class SolicitacaoChat {
        private String mensagem;
        private int qtdMusicos;
        private List<MensagemDTO> historico;

        public String getMensagem() { return mensagem; }
        public void setMensagem(String mensagem) { this.mensagem = mensagem; }

        public int getQtdMusicos() { return qtdMusicos; }
        public void setQtdMusicos(int qtdMusicos) { this.qtdMusicos = qtdMusicos; }

        public List<MensagemDTO> getHistorico() { return historico; }
        public void setHistorico(List<MensagemDTO> historico) { this.historico = historico; }
    }
}