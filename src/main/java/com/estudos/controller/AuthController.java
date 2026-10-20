package com.estudos.controller;

import com.estudos.dto.CadastroUsuarioRequest;
import com.estudos.dto.PerfilCatalogoDTO;
import com.estudos.dto.DadosContratoRequest;
import com.estudos.dto.AtualizarPerfilRequest;
import com.estudos.entidade.UsuarioEntity;
import com.estudos.modelo.Formacao;
import com.estudos.repositorio.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String SESSION_USER_ID = "usuarioId";
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    private final UsuarioRepository usuarioRepository;

    public AuthController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrar(@RequestBody CadastroUsuarioRequest request) {
        if (isBlank(request.getNome()) || isBlank(request.getEmail())
                || isBlank(request.getSenha()) || isBlank(request.getTipoPerfil())) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "Nome, e-mail, senha e perfil são obrigatórios."));
        }
        if (request.getSenha().length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "A senha deve ter pelo menos 8 caracteres."));
        }

        String tipoPerfil = request.getTipoPerfil().trim().toUpperCase(Locale.ROOT);
        if (!tipoPerfil.equals("ARTISTA") && !tipoPerfil.equals("ESTABELECIMENTO")) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "Perfil deve ser ARTISTA ou ESTABELECIMENTO."));
        }
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensagem", "E-mail já cadastrado no sistema."));
        }
        if (tipoPerfil.equals("ARTISTA")
                && (Formacao.fromQuantidadeMusicos(request.getQuantidadeMusicos() == null ? 0 : request.getQuantidadeMusicos()) == null
                || isBlank(request.getGeneroMusical()) || isBlank(request.getCidade()))) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "Artista precisa informar formação, gênero musical e cidade."));
        }
        if (tipoPerfil.equals("ESTABELECIMENTO")
                && (isBlank(request.getEstabelecimento()) || isBlank(request.getCidade())
                || isBlank(request.getGeneroDesejado()) || isBlank(request.getTipoEstabelecimento())
                || request.getPossuiEquipamentoSom() == null)) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "Estabelecimento precisa informar nome, tipo, cidade, gêneros desejados e disponibilidade de som."));
        }

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setNome(request.getNome().trim());
        usuario.setNomeLegal(trimToNull(request.getNomeLegal()));
        usuario.setEmail(email);
        usuario.setSenha(PASSWORD_ENCODER.encode(request.getSenha()));
        usuario.setTipoPerfil(tipoPerfil);
        usuario.setGeneroMusical(trimToNull(request.getGeneroMusical()));
        usuario.setEstabelecimento(trimToNull(request.getEstabelecimento()));
        usuario.setEndereco(trimToNull(request.getEndereco()));
        usuario.setCidade(trimToNull(request.getCidade()));
        usuario.setDescricao(trimToNull(request.getDescricao()));
        usuario.setQuantidadeMusicos(request.getQuantidadeMusicos());
        usuario.setGeneroDesejado(trimToNull(request.getGeneroDesejado()));
        usuario.setPossuiEquipamentoSom(request.getPossuiEquipamentoSom());
        usuario.setTipoEstabelecimento(trimToNull(request.getTipoEstabelecimento()));
        usuario.setCpfCnpj(trimToNull(request.getCpfCnpj()));

        UsuarioEntity salvo = usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(PerfilCatalogoDTO.from(salvo));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciais, HttpSession session) {
        String email = credenciais.get("email");
        String senha = credenciais.get("senha");
        if (isBlank(email) || isBlank(senha)) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "E-mail e senha são obrigatórios."));
        }

        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT));
        if (usuarioOpt.isEmpty() || !verificarSenha(usuarioOpt.get(), senha)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensagem", "E-mail ou senha inválidos."));
        }

        UsuarioEntity usuario = usuarioOpt.get();
        session.setAttribute(SESSION_USER_ID, usuario.getId());
        return ResponseEntity.ok(PerfilCatalogoDTO.from(usuario));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Object id = session.getAttribute(SESSION_USER_ID);
        if (!(id instanceof Long usuarioId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensagem", "Sessão não autenticada."));
        }
        return usuarioRepository.findById(usuarioId)
                .<ResponseEntity<?>>map(usuario -> ResponseEntity.ok(PerfilCatalogoDTO.from(usuario)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("mensagem", "Usuário da sessão não foi encontrado.")));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/dados-contrato")
    public ResponseEntity<?> atualizarDadosContrato(
            @RequestBody DadosContratoRequest request,
            HttpSession session) {
        Object id = session.getAttribute(SESSION_USER_ID);
        if (!(id instanceof Long usuarioId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensagem", "Sessão não autenticada."));
        }
        if (request == null || isBlank(request.nomeLegal()) || isBlank(request.cpfCnpj()) || isBlank(request.endereco())) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "Nome legal, CPF/CNPJ e endereço são obrigatórios."));
        }
        UsuarioEntity usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalStateException("Usuário da sessão não foi encontrado."));
        usuario.setNomeLegal(request.nomeLegal().trim());
        usuario.setCpfCnpj(request.cpfCnpj().trim());
        usuario.setEndereco(request.endereco().trim());
        usuarioRepository.save(usuario);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/perfil")
    public ResponseEntity<?> atualizarPerfil(
            @RequestBody AtualizarPerfilRequest request,
            HttpSession session) {
        Object id = session.getAttribute(SESSION_USER_ID);
        if (!(id instanceof Long usuarioId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensagem", "Sessão não autenticada."));
        }
        if (request == null || isBlank(request.nome()) || isBlank(request.tipoPerfil())) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "Nome e perfil são obrigatórios."));
        }
        String tipoPerfil = request.tipoPerfil().trim().toUpperCase(Locale.ROOT);
        if (!tipoPerfil.equals("ARTISTA") && !tipoPerfil.equals("ESTABELECIMENTO")) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "Perfil deve ser ARTISTA ou ESTABELECIMENTO."));
        }
        if (tipoPerfil.equals("ARTISTA")
                && (Formacao.fromQuantidadeMusicos(request.quantidadeMusicos() == null ? 0 : request.quantidadeMusicos()) == null
                || isBlank(request.generoMusical()) || isBlank(request.cidade()))) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "Artista precisa informar formação, gênero musical e cidade."));
        }
        if (tipoPerfil.equals("ESTABELECIMENTO")
                && (isBlank(request.estabelecimento()) || isBlank(request.cidade())
                || isBlank(request.generoDesejado()) || isBlank(request.tipoEstabelecimento())
                || request.possuiEquipamentoSom() == null)) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "Estabelecimento precisa informar nome, tipo, cidade, gêneros desejados e disponibilidade de som."));
        }
        UsuarioEntity usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Usuário da sessão não foi encontrado."));
        usuario.setNome(request.nome().trim());
        usuario.setNomeLegal(trimToNull(request.nomeLegal()));
        usuario.setTipoPerfil(tipoPerfil);
        usuario.setGeneroMusical(trimToNull(request.generoMusical()));
        usuario.setEstabelecimento(trimToNull(request.estabelecimento()));
        usuario.setEndereco(trimToNull(request.endereco()));
        usuario.setCidade(trimToNull(request.cidade()));
        usuario.setDescricao(trimToNull(request.descricao()));
        usuario.setQuantidadeMusicos(request.quantidadeMusicos());
        usuario.setGeneroDesejado(trimToNull(request.generoDesejado()));
        usuario.setPossuiEquipamentoSom(request.possuiEquipamentoSom());
        usuario.setTipoEstabelecimento(trimToNull(request.tipoEstabelecimento()));
        usuario.setCpfCnpj(trimToNull(request.cpfCnpj()));
        return ResponseEntity.ok(PerfilCatalogoDTO.from(usuarioRepository.save(usuario)));
    }

    private boolean verificarSenha(UsuarioEntity usuario, String senha) {
        String armazenada = usuario.getSenha();
        if (armazenada == null) {
            return false;
        }
        if (armazenada.startsWith("$2a$") || armazenada.startsWith("$2b$") || armazenada.startsWith("$2y$")) {
            return PASSWORD_ENCODER.matches(senha, armazenada);
        }
        if (armazenada.equals(senha)) {
            usuario.setSenha(PASSWORD_ENCODER.encode(senha));
            usuarioRepository.save(usuario);
            return true;
        }
        return false;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
