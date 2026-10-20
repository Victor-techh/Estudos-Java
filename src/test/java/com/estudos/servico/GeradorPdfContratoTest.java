package com.estudos.servico;

import com.estudos.entidade.ContratoEntity;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GeradorPdfContratoTest {

    @Test
    void generatesReadableContractPdfWithMinimumFeeAndAcceptanceTerms() throws Exception {
        ContratoEntity contrato = new ContratoEntity();
        contrato.setId(1L);
        contrato.setStatus("AGUARDANDO_ACEITES");
        contrato.setContratante("Bar Teste");
        contrato.setNomeLegalContratante("Bar Teste LTDA");
        contrato.setCpfCnpjContratante("00.000.000/0001-00");
        contrato.setEnderecoContratante("Fortaleza");
        contrato.setEmailContratante("bar@example.com");
        contrato.setContratado("Artista Teste");
        contrato.setNomeLegalContratado("Artista Teste");
        contrato.setCpfCnpjContratado("000.000.000-00");
        contrato.setEnderecoContratado("Fortaleza");
        contrato.setEmailContratado("artista@example.com");
        contrato.setDataHoraApresentacao(LocalDateTime.of(2030, 12, 20, 20, 0));
        contrato.setDataPagamento(LocalDate.of(2030, 12, 22));
        contrato.setDuracaoHoras(3);
        contrato.setFormacao("Solo");
        contrato.setQuantidadeMusicos(1);
        contrato.setValor(400.0);
        contrato.setValorLiquidoArtista(400.0);
        contrato.setEstabelecimentoPossuiSom(true);

        byte[] pdf = new GeradorPdfContrato().gerar(contrato);
        try (var documento = Loader.loadPDF(pdf)) {
            String texto = new PDFTextStripper().getText(documento);
            assertTrue(texto.contains("CONTRATO DE PRESTACAO"));
            assertTrue(texto.contains("400"));
            assertTrue(texto.contains("qualificada"), texto);
        }
    }
}
