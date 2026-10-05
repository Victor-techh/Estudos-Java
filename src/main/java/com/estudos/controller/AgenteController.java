package com.estudos.controller;

import com.estudos.dto.AtendimentoRequestDTO;
import com.estudos.servico.AgenteVendasService;
import org.springframework.web.bind.annotation.*;

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
    public String conversar(@RequestBody AtendimentoRequestDTO request) {
        if (request.getUsuarioId() != null) {
            return agenteVendasService.processarAtendimentoPorUsuario(
                    request.getUsuarioId(),
                    request.getMensagem(),
                    request.getHistorico(),
                    request.getQuantidadeMusicos()
            );
        }

        if (request.getEstabelecimento() != null) {
            return agenteVendasService.processarAtendimento(
                    request.getEstabelecimento(),
                    request.getMensagem(),
                    request.getHistorico(),
                    request.getQuantidadeMusicos()
            );
        }

        return "[ERRO]: Usuário não identificado para iniciar o atendimento.";
    }
}