package com.estudos.servico;

import com.estudos.entidade.UsuarioEntity;
import com.estudos.repositorio.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SessaoUsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final Set<String> administradores;

    public SessaoUsuarioService(
            UsuarioRepository usuarioRepository,
            @Value("${app.admin.emails:}") String emailsAdministradores) {
        this.usuarioRepository = usuarioRepository;
        this.administradores = Arrays.stream(emailsAdministradores.split(","))
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    public UsuarioEntity usuarioAutenticado(HttpSession session) {
        Object id = session.getAttribute("usuarioId");
        if (!(id instanceof Long usuarioId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autentique-se para continuar.");
        }
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário da sessão não existe."));
    }

    public UsuarioEntity administradorAutenticado(HttpSession session) {
        UsuarioEntity usuario = usuarioAutenticado(session);
        if (!administradores.contains(usuario.getEmail().toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ação permitida apenas para administradores.");
        }
        return usuario;
    }
}
