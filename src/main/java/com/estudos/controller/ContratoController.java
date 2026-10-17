package com.estudos.controller;

import com.estudos.entidade.ContratoEntity;
import com.estudos.entidade.ContratoHistoricoEntity;
import com.estudos.entidade.UsuarioEntity;
import com.estudos.repositorio.ContratoHistoricoRepository;
import com.estudos.repositorio.ContratoRepository;
import com.estudos.repositorio.UsuarioRepository;
import com.estudos.servico.EmailContratoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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
    private final UsuarioRepository usuarioRepository;

    public ContratoController(ContratoRepository contratoRepository, 
                              ContratoHistoricoRepository contratoHistoricoRepository, 
                              EmailContratoService emailContratoService,
                              UsuarioRepository usuarioRepository) {
        this.contratoRepository = contratoRepository;
        this.contratoHistoricoRepository = contratoHistoricoRepository;
        this.emailContratoService = emailContratoService;
        this.usuarioRepository = usuarioRepository;
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
    public ResponseEntity<List<ContratoHistoricoEntity>> buscarHistorico(
            @RequestParam(required = false) String usuarioId,
            Authentication authentication) {

        Long idResolvido = resolverUsuarioId(usuarioId, authentication);
        if (idResolvido == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<ContratoHistoricoEntity> historico = contratoHistoricoRepository.findByUsuarioId(idResolvido);
        return ResponseEntity.ok(historico);
    }

    /**
     * Dispara o salvamento do histórico associado ao usuarioId e aciona o envio.
     */
    @PostMapping("/enviar")
    public ResponseEntity<Void> enviarContrato(
            @RequestParam(required = false) String usuarioId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String estabelecimento,
            @RequestBody String textoContrato,
            Authentication authentication) {

        Long idResolvido = resolverUsuarioId(usuarioId, authentication);
        if (idResolvido == null) {
            idResolvido = 1L;
        }

        String emailFinal = (email != null && !email.isBlank()) ? email : (authentication != null ? authentication.getName() : "contratos@cura.com");
        String estabFinal = (estabelecimento != null && !estabelecimento.isBlank()) ? estabelecimento : "Estabelecimento Parceiro";

        emailContratoService.processarENotificarContrato(idResolvido, emailFinal, estabFinal, textoContrato);
        return ResponseEntity.ok().build();
    }

    private Long resolverUsuarioId(String usuarioId, Authentication authentication) {
        if (usuarioId != null && !usuarioId.isBlank() && !"null".equalsIgnoreCase(usuarioId) && !"undefined".equalsIgnoreCase(usuarioId)) {
            try {
                return Long.parseLong(usuarioId);
            } catch (NumberFormatException e) {
                // usuarioId pode ser um UID do Firebase
                if (usuarioRepository != null) {
                    UsuarioEntity u = usuarioRepository.findByUid(usuarioId).orElse(null);
                    if (u != null) {
                        return u.getId();
                    }
                }
            }
        }

        if (authentication == null) {
            authentication = SecurityContextHolder.getContext().getAuthentication();
        }

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal()) && usuarioRepository != null) {
            String identifier = authentication.getName();
            UsuarioEntity u = usuarioRepository.findByEmail(identifier)
                    .or(() -> usuarioRepository.findByUid(identifier))
                    .orElse(null);
            if (u != null) {
                return u.getId();
            }
        }

        return null;
    }
}