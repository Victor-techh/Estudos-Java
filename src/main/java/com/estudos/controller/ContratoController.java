package com.estudos.controller;

import com.estudos.entidade.ContratoEntity;
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
    private final EmailContratoService emailContratoService;

    public ContratoController(ContratoRepository contratoRepository, EmailContratoService emailContratoService) {
        this.contratoRepository = contratoRepository;
        this.emailContratoService = emailContratoService;
    }

    /**
     * Retorna a lista completa de contratos fechados via negociação com a IA.
     */
    @GetMapping
    public List<ContratoEntity> listarContratos() {
        return contratoRepository.findAll();
    }

    /**
     * Dispara o salvamento do histórico local, banco de dados e envio do e-mail.
     */
    @PostMapping("/enviar")
    public ResponseEntity<Void> enviarContrato(
            @RequestParam String email,
            @RequestParam String estabelecimento,
            @RequestBody String textoContrato) {
        
        emailContratoService.processarENotificarContrato(email, estabelecimento, textoContrato);
        return ResponseEntity.ok().build();
    }
}