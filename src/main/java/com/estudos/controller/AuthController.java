package com.estudos.controller;

import com.estudos.entidade.UsuarioEntity;
import com.estudos.repositorio.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensagem", "E-mail já cadastrado no sistema."));
        }
        UsuarioEntity novoUsuario = usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoUsuario);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciais) {
        String email = credenciais.get("email");
        String senha = credenciais.get("senha");

        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.findByEmailAndSenha(email, senha);

        if (usuarioOpt.isPresent()) {
            return ResponseEntity.ok(usuarioOpt.get());
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("mensagem", "E-mail ou senha inválidos."));
    }
}