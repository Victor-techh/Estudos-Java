package com.estudos.modelo;

import com.estudos.excecao.InfracaoLegalException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FormacaoTest {

    @Test
    void exposesOnlyTheThreeConfiguredFormationsAndMinimumFees() {
        assertEquals(400.0, Formacao.SOLO.getCacheMinimo());
        assertEquals(800.0, Formacao.DUO.getCacheMinimo());
        assertEquals(1200.0, Formacao.TRIO.getCacheMinimo());
        assertEquals(Formacao.DUO, Formacao.fromQuantidadeMusicos(2));
        org.junit.jupiter.api.Assertions.assertNull(Formacao.fromQuantidadeMusicos(4));
    }

    @Test
    void soloContractCannotBeCreatedBelowTheConfiguredMinimum() {
        Artista artista = new ArtistaSolo("Artista", "MPB", 399.99);
        Estabelecimento local = new Estabelecimento("Local", "Bar", "Fortaleza", 2000, "MPB", true);
        Proposta proposta = new Proposta(artista, local);

        assertThrows(InfracaoLegalException.class, () -> new Contrato(proposta));
    }

    @Test
    void duoAndTrioUseThePerMusicianMinimum() throws InfracaoLegalException {
        Artista duo = new Banda("Duo", "MPB", 800, 0, 2);
        Artista trio = new Banda("Trio", "MPB", 1200, 0, 3);
        Estabelecimento local = new Estabelecimento("Local", "Bar", "Fortaleza", 3000, "MPB", true);

        assertEquals(800.0, duo.getCacheMinimo());
        assertEquals(1200.0, trio.getCacheMinimo());
        new Contrato(new Proposta(duo, local));
        new Contrato(new Proposta(trio, local));
    }

    @Test
    void logisticsFeeCannotMaskAnArtisticFeeBelowTheMinimum() {
        Artista duo = new Banda("Duo", "MPB", 700, 100, 2);
        Estabelecimento local = new Estabelecimento("Local", "Bar", "Fortaleza", 900, "MPB", true);

        assertThrows(InfracaoLegalException.class, () -> new Contrato(new Proposta(duo, local)));
    }
}
