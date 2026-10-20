package com.estudos.dto;

import com.estudos.entidade.InteresseEntity;
import com.estudos.entidade.UsuarioEntity;

public record InteresseResponse(
        Long id,
        String status,
        boolean aceiteArtista,
        boolean aceiteEstabelecimento,
        PerfilCatalogoDTO contraparte,
        String emailContraparte,
        String enderecoContraparte,
        String cpfCnpjContraparte
) {
    public static InteresseResponse from(InteresseEntity interesse, UsuarioEntity contraparte, boolean contatosLiberados) {
        return new InteresseResponse(
                interesse.getId(),
                interesse.getStatus(),
                interesse.isAceiteArtista(),
                interesse.isAceiteEstabelecimento(),
                PerfilCatalogoDTO.from(contraparte),
                contatosLiberados ? contraparte.getEmail() : null,
                contatosLiberados ? contraparte.getEndereco() : null,
                contatosLiberados ? contraparte.getCpfCnpj() : null
        );
    }
}
