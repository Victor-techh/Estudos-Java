package com.estudos.controller;

import com.estudos.dto.MensagemDTO;
import com.estudos.modelo.Estabelecimento;
import com.estudos.modelo.UsuarioEntity;
import com.estudos.repositorio.UsuarioRepository;
import com.estudos.servico.AgenteVendasService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    public String conversar(@RequestBody SolicitacaoChat request) {
        List<String> artistasCadastrados = usuarioRepository.findAll().stream()
                .filter(u -> "ARTISTA".equalsIgnoreCase(u.getTipoPerfil()))
                .map(UsuarioEntity::getNome)
                .collect(Collectors.toList());

        Estabelecimento est = new Estabelecimento(
                "Estabelecimento Conectado",
                "Bar / Casa de Shows",
                "Endereço Local",
                0.0,
                "Variado",
                true
        );

        return agenteVendasService.processarAtendimento(
                est,
                request.getMensagem(),
                request.getHistorico(),
                request.getQtdMusicos(),
                artistasCadastrados
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