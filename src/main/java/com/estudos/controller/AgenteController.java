package com.estudos.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.estudos.dto.AtendimentoRequestDTO;
import com.estudos.dto.MensagemDTO;
import com.estudos.entidade.UsuarioEntity;
import com.estudos.repositorio.UsuarioRepository;
import com.estudos.servico.AgenteVendasService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping({"/api/agente", ""})
public class AgenteController {

    private final AgenteVendasService agenteVendasService;
    private final UsuarioRepository usuarioRepository;

    public AgenteController(AgenteVendasService agenteVendasService, UsuarioRepository usuarioRepository) {
        this.agenteVendasService = agenteVendasService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/chat")
    public String conversar(
            @RequestBody(required = false) AtendimentoRequestDTO request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        UsuarioEntity usuario = null;

        // 1. Tenta identificar o usuário autenticado via token Firebase
        if (authentication == null) {
            authentication = SecurityContextHolder.getContext().getAuthentication();
        }

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String email = null;
            String uid = null;
            String nome = null;

            Object principal = authentication.getPrincipal();
            if (principal instanceof String pStr) {
                if (pStr.contains("@")) {
                    email = pStr;
                } else {
                    uid = pStr;
                }
            }

            Object details = authentication.getDetails();
            if (details instanceof Map<?, ?> map) {
                if (map.get("email") instanceof String dEmail) email = dEmail;
                if (map.get("uid") instanceof String dUid) uid = dUid;
                if (map.get("name") instanceof String dName) nome = dName;
            }

            if (httpRequest != null) {
                if (email == null && httpRequest.getAttribute("firebaseEmail") instanceof String reqEmail) email = reqEmail;
                if (uid == null && httpRequest.getAttribute("firebaseUid") instanceof String reqUid) uid = reqUid;
                if (nome == null && httpRequest.getAttribute("firebaseName") instanceof String reqName) nome = reqName;
            }

            if (usuarioRepository != null) {
                if (email != null && !email.isBlank()) {
                    usuario = usuarioRepository.findByEmail(email).orElse(null);
                }
                if (usuario == null && uid != null && !uid.isBlank()) {
                    usuario = usuarioRepository.findByUid(uid).orElse(null);
                }

                // Auto-provisionamento caso o usuário esteja autenticado no Firebase mas não conste na base H2 em memória
                if (usuario == null && (email != null || uid != null)) {
                    usuario = new UsuarioEntity();
                    usuario.setEmail(email);
                    usuario.setUid(uid);
                    usuario.setNome(nome != null && !nome.isBlank() ? nome : (email != null && email.contains("@") ? email.split("@")[0] : "Usuário"));
                    usuario.setTipoPerfil("ESTABELECIMENTO");
                    usuario = usuarioRepository.save(usuario);
                } else if (usuario != null && usuario.getUid() == null && uid != null) {
                    usuario.setUid(uid);
                    usuario = usuarioRepository.save(usuario);
                }
            }
        }

        // 2. Se não identificado pelo contexto, tenta via payload do request
        if (usuario == null && request != null && usuarioRepository != null) {
            if (request.getUsuarioId() != null) {
                usuario = usuarioRepository.findById(request.getUsuarioId()).orElse(null);
            }
            if (usuario == null && request.getUid() != null && !request.getUid().isBlank()) {
                usuario = usuarioRepository.findByUid(request.getUid()).orElse(null);
            }
        }

        // 3. Processa a mensagem se o usuário foi localizado/criado
        if (usuario != null) {
            String mensagem = request != null ? request.getMensagem() : "";
            List<MensagemDTO> historico = request != null ? request.getHistorico() : null;
            int quantidadeMusicos = request != null ? request.getQuantidadeMusicos() : 1;

            return agenteVendasService.processarAtendimentoPorUsuario(
                    usuario,
                    mensagem,
                    historico,
                    quantidadeMusicos
            );
        }

        // 4. Fallback legado se vier apenas estabelecimento no DTO
        if (request != null && request.getEstabelecimento() != null) {
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