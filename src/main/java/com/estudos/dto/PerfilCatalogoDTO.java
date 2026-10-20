package com.estudos.dto;

import com.estudos.entidade.UsuarioEntity;
import com.estudos.modelo.Formacao;

public record PerfilCatalogoDTO(
        Long id,
        String nome,
        String tipoPerfil,
        String generoMusical,
        Integer quantidadeMusicos,
        String cidade,
        String descricao,
        String estabelecimento,
        String tipoEstabelecimento,
        String generoDesejado,
        Boolean possuiEquipamentoSom,
        Double cacheMinimo
) {
    public static PerfilCatalogoDTO from(UsuarioEntity usuario) {
        Double cacheMinimo = null;
        if ("ARTISTA".equals(usuario.getTipoPerfil()) && usuario.getQuantidadeMusicos() != null) {
            Formacao formacao = Formacao.fromQuantidadeMusicos(usuario.getQuantidadeMusicos());
            if (formacao != null) {
                cacheMinimo = formacao.getCacheMinimo();
            }
        }
        return new PerfilCatalogoDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getTipoPerfil(),
                usuario.getGeneroMusical(),
                usuario.getQuantidadeMusicos(),
                usuario.getCidade(),
                usuario.getDescricao(),
                usuario.getEstabelecimento(),
                usuario.getTipoEstabelecimento(),
                usuario.getGeneroDesejado(),
                usuario.getPossuiEquipamentoSom(),
                cacheMinimo
        );
    }
}
