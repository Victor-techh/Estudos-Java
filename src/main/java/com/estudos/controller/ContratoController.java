package com.estudos.controller;

import com.estudos.entidade.ContratoEntity;
import com.estudos.repositorio.ContratoRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller REST responsável por expor os dados dos contratos gravados no banco H2.
 */
@RestController
@RequestMapping("/api/contratos")
public class ContratoController {

    private final ContratoRepository contratoRepository;

    public ContratoController(ContratoRepository contratoRepository) {
        this.contratoRepository = contratoRepository;
    }

    /**
     * Retorna a lista completa de contratos fechados via negociação com a IA.
     */
    @GetMapping
    public List<ContratoEntity> listarContratos() {
        return contratoRepository.findAll();
    }
}