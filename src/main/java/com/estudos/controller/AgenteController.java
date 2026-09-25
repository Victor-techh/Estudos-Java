package com.estudos.controller;

import com.estudos.dto.MensagemDTO;
import com.estudos.modelo.Estabelecimento;
import com.estudos.modelo.Usuario;
import com.estudos.repositorio.UsuarioRepository;
import com.estudos.servico.AgenteVendasService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/agente")
public class AgenteController {

    // Serviço responsável pela comunicação com a API do Gemini e regras de negócio
    private final AgenteVendasService agenteVendasService;
    
    // Repositório JPA para consultar os registros salvos na tabela de usuários do H2
    private final UsuarioRepository usuarioRepository;

    // Injeção de dependências do Spring via construtor
    public AgenteController(AgenteVendasService agenteVendasService, UsuarioRepository usuarioRepository) {
        this.agenteVendasService = agenteVendasService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/chat")
    public String conversar(@RequestBody SolicitacaoChat request) {
        // 1. Consulta o banco H2, busca todos os registros e filtra apenas os nomes dos usuários com perfil 'ARTISTA'
        List<String> artistasCadastrados = usuarioRepository.findAll().stream()
                .filter(u -> "ARTISTA".equalsIgnoreCase(u.getTipoPerfil()))
                .map(Usuario::getNome)
                .collect(Collectors.toList());

        // 2. Cria objeto temporário com dados do estabelecimento para contexto da negociação
        Estabelecimento est = new Estabelecimento(
                "Estabelecimento Conectado",
                "Bar / Casa de Shows",
                "Endereço Local",
                0.0,
                "Variado",
                true
        );

        // 3. Chama o serviço da IA repassando a mensagem, o histórico e a lista de artistas extraída do H2
        return agenteVendasService.processarAtendimento(
                est,
                request.getMensagem(),
                request.getHistorico(),
                request.getQtdMusicos(),
                artistasCadastrados
        );
    }

    // Estrutura interna (DTO) para mapear o JSON recebido no corpo do POST
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