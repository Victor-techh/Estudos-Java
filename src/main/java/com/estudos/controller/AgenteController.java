package com.estudos.controller;

import com.estudos.dto.AtendimentoRequestDTO;
import com.estudos.dto.MensagemDTO;
import com.estudos.entidade.InteresseEntity;
import com.estudos.entidade.UsuarioEntity;
import com.estudos.modelo.Estabelecimento;
import com.estudos.repositorio.InteresseRepository;
import com.estudos.repositorio.UsuarioRepository;
import com.estudos.servico.AgenteVendasService;
import com.estudos.servico.SessaoUsuarioService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/agente")
public class AgenteController {

    private final AgenteVendasService agenteVendasService;
    private final InteresseRepository interesseRepository;
    private final UsuarioRepository usuarioRepository;
    private final SessaoUsuarioService sessaoUsuarioService;
    private final ObjectMapper objectMapper;

    public AgenteController(
            AgenteVendasService agenteVendasService,
            InteresseRepository interesseRepository,
            UsuarioRepository usuarioRepository,
            SessaoUsuarioService sessaoUsuarioService,
            ObjectMapper objectMapper) {
        this.agenteVendasService = agenteVendasService;
        this.interesseRepository = interesseRepository;
        this.usuarioRepository = usuarioRepository;
        this.sessaoUsuarioService = sessaoUsuarioService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/chat")
    public String conversar(@RequestBody AtendimentoRequestDTO request, HttpSession session) {
        UsuarioEntity usuario = sessaoUsuarioService.usuarioAutenticado(session);
        if (request.getMensagem() == null || request.getMensagem().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A mensagem não pode estar vazia.");
        }

        InteresseEntity interesse = request.getInteresseId() == null
                ? null
                : carregarInteresseMutuo(request.getInteresseId(), usuario.getId());
        List<MensagemDTO> historico = interesse == null
                ? normalizarHistorico(request.getHistorico())
                : lerHistorico(interesse.getHistoricoJson());
        if (!ultimaMensagemEhDoUsuario(historico, request.getMensagem())) {
            historico.add(new MensagemDTO("user", request.getMensagem()));
        }
        int quantidadeMusicos = request.getQuantidadeMusicos();
        if ("ARTISTA".equals(usuario.getTipoPerfil())) {
            quantidadeMusicos = usuario.getQuantidadeMusicos() == null ? 1 : usuario.getQuantidadeMusicos();
        } else if (interesse != null) {
            UsuarioEntity artista = usuarioRepository.findById(interesse.getArtistaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil do artista não encontrado."));
            quantidadeMusicos = artista.getQuantidadeMusicos() == null ? 1 : artista.getQuantidadeMusicos();
        }
        String resposta;
        if ("ARTISTA".equals(usuario.getTipoPerfil())) {
            resposta = agenteVendasService.processarAtendimentoPorUsuario(
                    usuario.getId(),
                    request.getMensagem(),
                    historico.subList(0, historico.size() - 1),
                    quantidadeMusicos
            );
        } else {
            Estabelecimento estabelecimento = new Estabelecimento(
                    usuario.getEstabelecimento() != null ? usuario.getEstabelecimento() : usuario.getNome(),
                    usuario.getTipoEstabelecimento(),
                    usuario.getEndereco(),
                    0,
                    usuario.getGeneroDesejado(),
                    Boolean.TRUE.equals(usuario.getPossuiEquipamentoSom())
            );
            resposta = agenteVendasService.processarAtendimento(
                    estabelecimento,
                    request.getMensagem(),
                    historico.subList(0, historico.size() - 1),
                    quantidadeMusicos
            );
        }

        if (interesse != null) {
            historico.add(new MensagemDTO("model", resposta));
            interesse.setHistoricoJson(escreverHistorico(historico));
            interesse.setAtualizadoEm(LocalDateTime.now());
            interesseRepository.save(interesse);
        }
        return resposta;
    }

    private InteresseEntity carregarInteresseMutuo(Long interesseId, Long usuarioId) {
        InteresseEntity interesse = interesseRepository.findById(interesseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interesse não encontrado."));
        boolean participante = usuarioId.equals(interesse.getArtistaId())
                || usuarioId.equals(interesse.getEstabelecimentoId());
        if (!participante) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esse interesse não pertence à sua conta.");
        }
        if (!"INTERESSE_MUTUO".equals(interesse.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A conversa só pode ser compartilhada após o interesse mútuo.");
        }
        return interesse;
    }

    private List<MensagemDTO> lerHistorico(String historicoJson) {
        if (historicoJson == null || historicoJson.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(historicoJson, new TypeReference<>() {});
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível carregar a conversa salva.", exception);
        }
    }

    private String escreverHistorico(List<MensagemDTO> historico) {
        try {
            return objectMapper.writeValueAsString(historico);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível salvar a conversa.", exception);
        }
    }

    private List<MensagemDTO> normalizarHistorico(List<MensagemDTO> historico) {
        return historico == null ? new ArrayList<>() : new ArrayList<>(historico);
    }

    private boolean ultimaMensagemEhDoUsuario(List<MensagemDTO> historico, String mensagem) {
        if (historico.isEmpty()) {
            return false;
        }
        MensagemDTO ultima = historico.get(historico.size() - 1);
        return ultima != null
                && "user".equalsIgnoreCase(ultima.getRole())
                && mensagem.equals(ultima.getTexto());
    }
}
