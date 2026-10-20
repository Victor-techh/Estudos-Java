package com.estudos.servico;

import com.estudos.entidade.ContratoEntity;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class GeradorPdfContrato {
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    public byte[] gerar(ContratoEntity contrato) throws IOException {
        try (PDDocument documento = new PDDocument(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font negrito = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            List<String> linhas = montarLinhas(contrato);
            int linhaAtual = 0;
            int trechoAtual = 0;
            while (linhaAtual < linhas.size()) {
                PDPage pagina = new PDPage(PDRectangle.A4);
                documento.addPage(pagina);
                float y = 790;
                try (PDPageContentStream conteudo = new PDPageContentStream(documento, pagina)) {
                    while (linhaAtual < linhas.size() && y >= 55) {
                        String linha = linhas.get(linhaAtual);
                        List<String> trechos = quebrar(linha, 94);
                        while (trechoAtual < trechos.size() && y >= 55) {
                            conteudo.beginText();
                            conteudo.setFont(linhaAtual == 0 ? negrito : regular, linhaAtual == 0 ? 16 : 10);
                            conteudo.newLineAtOffset(50, y);
                            conteudo.showText(trechos.get(trechoAtual));
                            conteudo.endText();
                            y -= linhaAtual == 0 ? 28 : 17;
                            trechoAtual++;
                        }
                        if (trechoAtual == trechos.size()) {
                            if (linha.isEmpty()) {
                                y -= 8;
                            }
                            linhaAtual++;
                            trechoAtual = 0;
                        }
                    }
                }
            }
            documento.save(saida);
            return saida.toByteArray();
        }
    }

    private List<String> montarLinhas(ContratoEntity contrato) {
        List<String> linhas = new ArrayList<>();
        linhas.add("CONTRATO DE PRESTACAO DE SERVICOS ARTISTICOS");
        linhas.add("");
        linhas.add("Numero: " + contrato.getId());
        linhas.add("Status: " + contrato.getStatus());
        linhas.add("CONTRATANTE: " + contrato.getContratante());
        linhas.add("Nome civil/razao social: " + contrato.getNomeLegalContratante());
        linhas.add("CPF/CNPJ: " + contrato.getCpfCnpjContratante());
        linhas.add("Endereco: " + contrato.getEnderecoContratante());
        linhas.add("E-mail: " + contrato.getEmailContratante());
        linhas.add("");
        linhas.add("ARTISTA/CONTRATADO: " + contrato.getContratado());
        linhas.add("Nome civil/razao social: " + contrato.getNomeLegalContratado());
        linhas.add("CPF/CNPJ: " + contrato.getCpfCnpjContratado());
        linhas.add("Endereco: " + contrato.getEnderecoContratado());
        linhas.add("E-mail: " + contrato.getEmailContratado());
        linhas.add("");
        linhas.add("APRESENTACAO");
        linhas.add("Data e hora: " + contrato.getDataHoraApresentacao().format(DATA_HORA));
        linhas.add("Duracao: " + contrato.getDuracaoHoras() + " hora(s)");
        linhas.add("Formacao: " + contrato.getFormacao() + " (" + contrato.getQuantidadeMusicos() + " musico(s))");
        linhas.add("");
        linhas.add("VALORES");
        linhas.add("Cache bruto acordado: " + moeda(contrato.getValor()));
        if (contrato.getDescontoCancelamento() != null && contrato.getDescontoCancelamento() > 0) {
            linhas.add("Credito por cancelamento anterior aplicado ao estabelecimento: -" + moeda(contrato.getDescontoCancelamento()));
        }
        linhas.add("Valor liquido do cache para o artista: " + moeda(contrato.getValorLiquidoArtista()));
        if (contrato.getMultaCancelamentoContratante() != null && contrato.getMultaCancelamentoContratante() > 0) {
            linhas.add("Multa de cancelamento do contratante devida ao artista: " + moeda(contrato.getMultaCancelamentoContratante()));
        }
        linhas.add("Pagamento ate: " + contrato.getDataPagamento().format(DATA));
        if (Boolean.TRUE.equals(contrato.getEstabelecimentoPossuiSom())) {
            linhas.add("Som: equipamento proprio do estabelecimento; sem cobranca de aluguel de som.");
        } else {
            linhas.add("Som: negociado separadamente. Valor/condicoes: " + contrato.getCondicoesSom());
            if (contrato.getValorSom() != null && contrato.getValorSom() > 0) {
                linhas.add("Valor do som: " + moeda(contrato.getValorSom()));
            }
        }
        linhas.add("");
        linhas.add("CANCELAMENTO");
        linhas.add("Cancelamento pelo contratante dentro das 48 horas anteriores: multa de 50% do cache bruto, devida ao artista.");
        linhas.add("Cancelamento pelo artista dentro das 48 horas sem justificativa medica aprovada: credito fixo de R$ 100,00 aplicado como desconto ao estabelecimento do proximo show. O registro negativo de credibilidade expira em 3 meses; credito financeiro pendente permanece ate compensacao/acordo.");
        if (contrato.getStatusCancelamento() != null) {
            linhas.add("Registro deste cancelamento: " + contrato.getStatusCancelamento());
        }
        linhas.add("");
        linhas.add("ACEITE NA PLATAFORMA");
        linhas.add("O aceite registrado abaixo comprova a manifestacao de concordancia nesta plataforma; nao e assinatura digital qualificada.");
        linhas.add("Aceite do contratante: " + (contrato.getAceiteContratanteEm() == null ? "pendente" : contrato.getAceiteContratanteEm().format(DATA_HORA)));
        linhas.add("Aceite do artista: " + (contrato.getAceiteArtistaEm() == null ? "pendente" : contrato.getAceiteArtistaEm().format(DATA_HORA)));
        linhas.add("Hash da minuta aceita: " + (contrato.getHashMinuta() == null ? "aguardando geracao" : contrato.getHashMinuta()));
        linhas.add("");
        linhas.add("REFERENCIA DE CACHES");
        linhas.add("Valor minimo informado pelo responsavel do projeto como referencia sindical do SINDIMUCE: R$ 400,00 por musico para ate 3 horas.");
        linhas.add("CLT e Lei Federal 3.857/1960 sao referencias legais informadas; nao se afirma que estabelecam os valores desta tabela.");
        linhas.add("Documento gerado para revisao pelas partes. Recomenda-se revisao juridica antes do uso como instrumento definitivo.");
        return linhas;
    }

    private String moeda(Double valor) {
        return valor == null ? "R$ 0,00" : MOEDA.format(valor).replace('\u00a0', ' ');
    }

    private List<String> quebrar(String texto, int tamanho) {
        if (texto.length() <= tamanho) {
            return List.of(texto);
        }
        List<String> partes = new ArrayList<>();
        String restante = texto;
        while (restante.length() > tamanho) {
            int corte = restante.lastIndexOf(' ', tamanho);
            if (corte <= 0) {
                corte = tamanho;
            }
            partes.add(restante.substring(0, corte));
            restante = restante.substring(corte).stripLeading();
        }
        if (!restante.isEmpty()) {
            partes.add(restante);
        }
        return partes;
    }
}
