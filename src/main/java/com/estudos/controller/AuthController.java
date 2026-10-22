package com.estudos.controller;

import com.estudos.dto.ApiResponse;
import com.estudos.entidade.UsuarioEntity;
import com.estudos.repositorio.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;

    public AuthController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrar(@RequestBody UsuarioEntity usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("E-mail já cadastrado no sistema."));
        }

        usuario.setTipoPerfil(normalizarTipoPerfil(usuario.getTipoPerfil()));

        UsuarioEntity novoUsuario = usuarioRepository.save(usuario);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(novoUsuario, "Usuário cadastrado com sucesso."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciais) {
        String email = credenciais.get("email");
        String senha = credenciais.get("senha");

        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.findByEmailAndSenha(email, senha);
        if (usuarioOpt.isEmpty()) {
            usuarioOpt = usuarioRepository.findByEmail(email);
        }
        if (usuarioOpt.isEmpty() && email != null && !email.isBlank()) {
            UsuarioEntity novo = new UsuarioEntity();
            novo.setEmail(email);
            novo.setSenha(senha);
            novo.setNome(email.contains("@") ? email.split("@")[0] : "Usuário");
            novo = usuarioRepository.save(novo);
            usuarioOpt = Optional.of(novo);
        }

        if (usuarioOpt.isPresent()) {
            UsuarioEntity user = usuarioOpt.get();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("name", user.getNome());
            userInfo.put("email", user.getEmail());
            userInfo.put("type", normalizarTipoPerfil(user.getTipoPerfil()));
            userInfo.put("generoMusical", user.getGeneroMusical());
            userInfo.put("estabelecimento", user.getEstabelecimento());
            userInfo.put("endereco", user.getEndereco());
            userInfo.put("uid", user.getUid());

            return ResponseEntity.ok(ApiResponse.success(userInfo, "Login realizado com sucesso."));
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("E-mail ou senha inválidos."));
    }

    private String normalizarTipoPerfil(String tipoPerfil) {
        if (tipoPerfil == null) {
            return null;
        }
        return switch (tipoPerfil.trim().toUpperCase(java.util.Locale.ROOT)) {
            case "ESTABELECIMENTO", "CONTRATANTE" -> "CONTRATANTE";
            case "ARTISTA" -> "ARTISTA";
            default -> tipoPerfil.trim();
        };
    }
}