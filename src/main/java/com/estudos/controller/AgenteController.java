package com.estudos.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.estudos.dto.AtendimentoRequestDTO;
import com.estudos.repositorio.UsuarioRepository;
import com.estudos.servico.AgenteVendasService;

@RestController
@RequestMapping("/api/agente")
public class AgenteController {

    private final AgenteVendasService agenteVendasService;
    private final UsuarioRepository usuarioRepository;

    public AgenteController(AgenteVendasService agenteVendasService, UsuarioRepository usuarioRepository) {
        this.agenteVendasService = agenteVendasService;
        this.usuarioRepository = usuarioRepository;
    }

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