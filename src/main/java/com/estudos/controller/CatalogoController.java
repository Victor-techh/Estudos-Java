package com.estudos.controller;

import com.estudos.dto.PerfilCatalogoDTO;
import com.estudos.repositorio.UsuarioRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {
    private final UsuarioRepository usuarioRepository;

    public CatalogoController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/artistas")
    public List<PerfilCatalogoDTO> listarArtistas() {
        return usuarioRepository.findByTipoPerfilOrderByNomeAsc("ARTISTA").stream()
                .map(PerfilCatalogoDTO::from)
                .toList();
    }

    @GetMapping("/estabelecimentos")
    public List<PerfilCatalogoDTO> listarEstabelecimentos() {
        return usuarioRepository.findByTipoPerfilOrderByNomeAsc("ESTABELECIMENTO").stream()
                .map(PerfilCatalogoDTO::from)
                .toList();
    }
}
