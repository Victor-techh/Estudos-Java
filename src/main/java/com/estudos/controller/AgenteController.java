package com.estudos.controller;

import com.estudos.dto.AtendimentoRequestDTO;
import com.estudos.servico.AgenteVendasService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agente")
public class AgenteController {

    private final AgenteVendasService agenteService;

    // Injeção de dependência via construtor pelo Spring
    public AgenteController(AgenteVendasService agenteService) {
        this.agenteService = agenteService;
    }

    @PostMapping("/atendimento")
    public String atenderCliente(@RequestBody AtendimentoRequestDTO dto) {
        return agenteService.processarAtendimento(
            dto.getEstabelecimento(), 
            dto.getMensagem(),
            dto.getHistorico(),
            dto.getQuantidadeMusicos()
        );
    }
}