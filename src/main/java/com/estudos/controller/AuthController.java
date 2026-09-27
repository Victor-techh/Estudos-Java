package com.estudos.controller;

import com.estudos.dto.ApiResponse;
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
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("E-mail já cadastrado no sistema."));
        }

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

        System.out.println(usuarioOpt);

        if (usuarioOpt.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(usuarioOpt.get(), "Login realizado com sucesso."));
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("E-mail ou senha inválidos."));
    }
}