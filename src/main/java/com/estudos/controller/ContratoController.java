package com.estudos.controller;

import com.estudos.entidade.ContratoEntity;
import com.estudos.entidade.ContratoHistoricoEntity;
import com.estudos.repositorio.ContratoHistoricoRepository;
import com.estudos.repositorio.ContratoRepository;
import com.estudos.servico.EmailContratoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST responsável por expor dados e acionar envio de contratos.
 */
@RestController
@RequestMapping("/api/contratos")
public class ContratoController {

    private final ContratoRepository contratoRepository;
    private final ContratoHistoricoRepository contratoHistoricoRepository;
    private final EmailContratoService emailContratoService;

    public ContratoController(ContratoRepository contratoRepository, 
                              ContratoHistoricoRepository contratoHistoricoRepository, 
                              EmailContratoService emailContratoService) {
        this.contratoRepository = contratoRepository;
        this.contratoHistoricoRepository = contratoHistoricoRepository;
        this.emailContratoService = emailContratoService;
    }

    /**
     * Retorna a lista completa de contratos no banco.
     */
    @GetMapping
    public List<ContratoEntity> listarContratos() {
        return contratoRepository.findAll();
    }

    /**
     * Retorna o histórico de contratos filtrado por usuário.
     */
    @GetMapping("/historico")
    public ResponseEntity<List<ContratoHistoricoEntity>> buscarHistorico(@RequestParam Long usuarioId) {
        List<ContratoHistoricoEntity> historico = contratoHistoricoRepository.findByUsuarioId(usuarioId);
        return ResponseEntity.ok(historico);
    }

    /**
     * Dispara o salvamento do histórico associado ao usuarioId e aciona o envio.
     */
    @PostMapping("/enviar")
    public ResponseEntity<Void> enviarContrato(
            @RequestParam Long usuarioId,
            @RequestParam String email,
            @RequestParam String estabelecimento,
            @RequestBody String textoContrato) {

        emailContratoService.processarENotificarContrato(usuarioId, email, estabelecimento, textoContrato);
        return ResponseEntity.ok().build();
    }
}