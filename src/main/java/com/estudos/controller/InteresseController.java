package com.estudos.controller;

import com.estudos.dto.InteresseRequest;
import com.estudos.dto.InteresseResponse;
import com.estudos.entidade.InteresseEntity;
import com.estudos.entidade.UsuarioEntity;
import com.estudos.repositorio.InteresseRepository;
import com.estudos.repositorio.UsuarioRepository;
import com.estudos.servico.SessaoUsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/interesses")
public class InteresseController {
    private static final String ARTISTA = "ARTISTA";
    private static final String ESTABELECIMENTO = "ESTABELECIMENTO";
    private static final String AGUARDANDO = "AGUARDANDO_INTERESSE";
    private static final String MUTUO = "INTERESSE_MUTUO";
    private static final String DESISTIDO = "DESISTIDO";

    private final InteresseRepository interesseRepository;
    private final UsuarioRepository usuarioRepository;
    private final SessaoUsuarioService sessaoUsuarioService;

    public InteresseController(
            InteresseRepository interesseRepository,
            UsuarioRepository usuarioRepository,
            SessaoUsuarioService sessaoUsuarioService) {
        this.interesseRepository = interesseRepository;
        this.usuarioRepository = usuarioRepository;
        this.sessaoUsuarioService = sessaoUsuarioService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InteresseResponse demonstrarInteresse(@RequestBody InteresseRequest request, HttpSession session) {
        UsuarioEntity atual = sessaoUsuarioService.usuarioAutenticado(session);
        if (request == null || request.perfilId() == null || request.perfilId().equals(atual.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe outro perfil para demonstrar interesse.");
        }
        UsuarioEntity alvo = usuarioRepository.findById(request.perfilId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil não encontrado."));
        boolean artistaInicia = ARTISTA.equals(atual.getTipoPerfil()) && ESTABELECIMENTO.equals(alvo.getTipoPerfil());
        boolean estabelecimentoInicia = ESTABELECIMENTO.equals(atual.getTipoPerfil()) && ARTISTA.equals(alvo.getTipoPerfil());
        if (!artistaInicia && !estabelecimentoInicia) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O interesse deve relacionar um artista e um estabelecimento.");
        }

        Long artistaId = artistaInicia ? atual.getId() : alvo.getId();
        Long estabelecimentoId = estabelecimentoInicia ? atual.getId() : alvo.getId();
        InteresseEntity interesse = interesseRepository.findByArtistaIdAndEstabelecimentoId(artistaId, estabelecimentoId)
                .orElseGet(InteresseEntity::new);
        if (interesse.getId() != null && !DESISTIDO.equals(interesse.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um interesse ativo entre esses perfis.");
        }
        interesse.setArtistaId(artistaId);
        interesse.setEstabelecimentoId(estabelecimentoId);
        interesse.setAceiteArtista(artistaInicia);
        interesse.setAceiteEstabelecimento(estabelecimentoInicia);
        interesse.setStatus(AGUARDANDO);
        interesse.setCriadoEm(LocalDateTime.now());
        interesse.setAtualizadoEm(LocalDateTime.now());
        interesse.setHistoricoJson("[]");
        InteresseEntity salvo = interesseRepository.save(interesse);
        return respostaPara(salvo, atual.getId());
    }

    @GetMapping
    public List<InteresseResponse> listar(HttpSession session) {
        UsuarioEntity atual = sessaoUsuarioService.usuarioAutenticado(session);
        return interesseRepository.findByArtistaIdOrEstabelecimentoId(atual.getId(), atual.getId()).stream()
                .filter(interesse -> !DESISTIDO.equals(interesse.getStatus()))
                .map(interesse -> respostaPara(interesse, atual.getId()))
                .toList();
    }

    @PostMapping("/{id}/aceite")
    public InteresseResponse aceitar(@PathVariable Long id, HttpSession session) {
        UsuarioEntity atual = sessaoUsuarioService.usuarioAutenticado(session);
        InteresseEntity interesse = carregarParticipacao(id, atual.getId());
        if (DESISTIDO.equals(interesse.getStatus())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Esse interesse foi encerrado.");
        }
        if (atual.getId().equals(interesse.getArtistaId())) {
            interesse.setAceiteArtista(true);
        } else {
            interesse.setAceiteEstabelecimento(true);
        }
        interesse.setStatus(interesse.isAceiteArtista() && interesse.isAceiteEstabelecimento() ? MUTUO : AGUARDANDO);
        interesse.setAtualizadoEm(LocalDateTime.now());
        return respostaPara(interesseRepository.save(interesse), atual.getId());
    }

    @PostMapping("/{id}/desistencia")
    public InteresseResponse desistir(@PathVariable Long id, HttpSession session) {
        UsuarioEntity atual = sessaoUsuarioService.usuarioAutenticado(session);
        InteresseEntity interesse = carregarParticipacao(id, atual.getId());
        interesse.setStatus(DESISTIDO);
        interesse.setAtualizadoEm(LocalDateTime.now());
        return respostaPara(interesseRepository.save(interesse), atual.getId());
    }

    private InteresseEntity carregarParticipacao(Long interesseId, Long usuarioId) {
        InteresseEntity interesse = interesseRepository.findById(interesseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interesse não encontrado."));
        if (!usuarioId.equals(interesse.getArtistaId()) && !usuarioId.equals(interesse.getEstabelecimentoId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esse interesse não pertence à sua conta.");
        }
        return interesse;
    }

    private InteresseResponse respostaPara(InteresseEntity interesse, Long usuarioId) {
        boolean artistaAtual = usuarioId.equals(interesse.getArtistaId());
        Long idContraparte = artistaAtual ? interesse.getEstabelecimentoId() : interesse.getArtistaId();
        UsuarioEntity contraparte = usuarioRepository.findById(idContraparte)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil relacionado não encontrado."));
        boolean contatosLiberados = MUTUO.equals(interesse.getStatus())
                && interesse.isAceiteArtista()
                && interesse.isAceiteEstabelecimento();
        return InteresseResponse.from(interesse, contraparte, contatosLiberados);
    }
}
