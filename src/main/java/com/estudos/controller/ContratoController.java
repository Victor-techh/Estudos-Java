package com.estudos.controller;

import com.estudos.dto.CancelamentoRequest;
import com.estudos.dto.CancelamentoResponse;
import com.estudos.dto.ContratoRequest;
import com.estudos.dto.ContratoResumoDTO;
import com.estudos.dto.RevisaoCancelamentoRequest;
import com.estudos.entidade.CancelamentoContratoEntity;
import com.estudos.entidade.ContratoEntity;
import com.estudos.entidade.ContratoHistoricoEntity;
import com.estudos.entidade.UsuarioEntity;
import com.estudos.repositorio.ContratoHistoricoRepository;
import com.estudos.repositorio.ContratoRepository;
import com.estudos.servico.ContratoService;
import com.estudos.servico.SessaoUsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contratos")
public class ContratoController {
    private final ContratoRepository contratoRepository;
    private final ContratoHistoricoRepository contratoHistoricoRepository;
    private final ContratoService contratoService;
    private final SessaoUsuarioService sessaoUsuarioService;

    public ContratoController(
            ContratoRepository contratoRepository,
            ContratoHistoricoRepository contratoHistoricoRepository,
            ContratoService contratoService,
            SessaoUsuarioService sessaoUsuarioService) {
        this.contratoRepository = contratoRepository;
        this.contratoHistoricoRepository = contratoHistoricoRepository;
        this.contratoService = contratoService;
        this.sessaoUsuarioService = sessaoUsuarioService;
    }

    @GetMapping
    public List<ContratoResumoDTO> listarContratos(HttpSession session) {
        UsuarioEntity atual = sessaoUsuarioService.usuarioAutenticado(session);
        return contratoRepository.findByArtistaIdOrEstabelecimentoId(atual.getId(), atual.getId()).stream()
                .map(ContratoResumoDTO::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<ContratoResumoDTO> criar(@RequestBody ContratoRequest request, HttpSession session) {
        UsuarioEntity atual = sessaoUsuarioService.usuarioAutenticado(session);
        ContratoEntity contrato = contratoService.criar(request, atual);
        return ResponseEntity.status(201).body(ContratoResumoDTO.from(contrato));
    }

    @PostMapping("/{id}/aceite")
    public ContratoResumoDTO aceitar(@PathVariable Long id, HttpSession session) {
        UsuarioEntity atual = sessaoUsuarioService.usuarioAutenticado(session);
        return ContratoResumoDTO.from(contratoService.aceitar(id, atual));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> baixarPdf(@PathVariable Long id, HttpSession session) {
        UsuarioEntity atual = sessaoUsuarioService.usuarioAutenticado(session);
        ContratoEntity contrato = contratoService.carregarContrato(id);
        contratoService.verificarParticipante(contrato, atual.getId());
        if (contrato.getPdf() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "PDF ainda não foi gerado.");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("contrato-" + id + ".pdf", StandardCharsets.UTF_8)
                .build());
        return ResponseEntity.ok().headers(headers).body(contrato.getPdf());
    }

    @PostMapping("/{id}/cancelamento")
    public CancelamentoResponse cancelar(
            @PathVariable Long id,
            @RequestBody(required = false) CancelamentoRequest request,
            HttpSession session) {
        UsuarioEntity atual = sessaoUsuarioService.usuarioAutenticado(session);
        return CancelamentoResponse.from(contratoService.cancelar(id, request, atual));
    }

    @GetMapping("/credibilidade")
    public Map<String, Integer> consultarCredibilidade(HttpSession session) {
        UsuarioEntity atual = sessaoUsuarioService.usuarioAutenticado(session);
        if (!"ARTISTA".equals(atual.getTipoPerfil())) {
            return Map.of("pontosNegativos", 0);
        }
        return Map.of("pontosNegativos", contratoService.pontosNegativosAtivos(atual.getId()));
    }

    @GetMapping("/admin/cancelamentos-pendentes")
    public List<CancelamentoResponse> cancelamentosPendentes(HttpSession session) {
        sessaoUsuarioService.administradorAutenticado(session);
        return contratoService.listarAguardandoRevisao().stream()
                .map(CancelamentoResponse::from)
                .toList();
    }

    @PostMapping("/admin/cancelamentos/{id}/revisao")
    public CancelamentoResponse revisarCancelamento(
            @PathVariable Long id,
            @RequestBody RevisaoCancelamentoRequest request,
            HttpSession session) {
        UsuarioEntity administrador = sessaoUsuarioService.administradorAutenticado(session);
        if (request == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Informe se a justificativa foi aprovada.");
        }
        CancelamentoContratoEntity cancelamento = contratoService.revisarCancelamento(id, request.aprovada(), administrador);
        return CancelamentoResponse.from(cancelamento);
    }

    @GetMapping("/historico")
    public ResponseEntity<List<ContratoHistoricoEntity>> buscarHistorico(HttpSession session) {
        UsuarioEntity atual = sessaoUsuarioService.usuarioAutenticado(session);
        return ResponseEntity.ok(contratoHistoricoRepository.findByUsuarioId(atual.getId()));
    }
}
