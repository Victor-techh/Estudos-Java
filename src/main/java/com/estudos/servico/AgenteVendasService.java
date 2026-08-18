package com.estudos.servico;

import com.estudos.dto.MensagemDTO;
import com.estudos.entidade.ContratoEntity;
import com.estudos.modelo.Estabelecimento;
import com.estudos.repositorio.ContratoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Service
public class AgenteVendasService {

    private final HttpClient httpClient;
    private final ContratoRepository contratoRepository;
    private static final double CACHE_BASE_POR_MUSICO = 420.0;

    // Construtor sem argumentos para compatibilidade com chamadas diretas fora do Spring (ex: Principal.java)
    public AgenteVendasService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.contratoRepository = null;
    }

    // Construtor utilizado pelo Spring Boot para injetar o Repositório
    @Autowired
    public AgenteVendasService(ContratoRepository contratoRepository) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.contratoRepository = contratoRepository;
    }

    // Sobrecarga para chamadas legadas com 2 parâmetros
    public String processarAtendimento(Estabelecimento est, String mensagem) {
        return processarAtendimento(est, mensagem, null, 1);
    }

    public String processarAtendimento(Estabelecimento est, String mensagemAtual, List<MensagemDTO> historico, int qtdMusicos) {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return "[ERRO CONFIGURAÇÃO]: GEMINI_API_KEY não definida.";
        }

        int totalIntegrantes = qtdMusicos > 0 ? qtdMusicos : 1;
        double cacheMinimoPiso = totalIntegrantes * CACHE_BASE_POR_MUSICO;

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=" + apiKey;

        String contextoComercial = String.format(
            "Você é um agente de vendas comercial de shows no Ceará. Responda de forma extremamente direta, concisa e objetiva. Evite cordialidades desnecessárias.\n\n" +
            "DADOS DA NEGOCIAÇÃO:\n" +
            "- Contratante: %s (%s)\n" +
            "- Endereço: %s\n" +
            "- Formação: %d músico(s)\n" +
            "- Estilo: %s\n" +
            "- Equipamento de Som da Casa: %b\n" +
            "- Piso Legal/Sindical (SINDIMUCE / Lei Estadual nº 15.112/2012-CE): R$ 420,00 por integrante (Total Mínimo: R$ %.2f)\n\n" +
            "DIRETRIZES DE RESPOSTA:\n" +
            "1. Se o valor oferecido for menor que R$ %.2f, RECUSE IMEDIATAMENTE. Cite explicitamente que o valor infringe o piso da Tabela de Cachês Mínimos do SINDIMUCE e a Lei Estadual nº 15.112/2012.\n" +
            "2. Se o valor for igual ou superior a R$ %.2f, ACEITE. Inclua obrigatoriamente a expressão 'Negócio fechado!' e resuma apenas os valores e condições.",
            est.getNome(), est.getTipo(), est.getEndereco(), totalIntegrantes, est.getEstiloMusicalDesejado(), est.isPossuiEquipamentoSom(),
            cacheMinimoPiso, cacheMinimoPiso, cacheMinimoPiso
        );

        StringBuilder contentsJson = new StringBuilder();
        contentsJson.append("[");
        contentsJson.append("{\"role\":\"user\",\"parts\":[{\"text\":\"").append(escaparTexto(contextoComercial)).append("\"}]},");
        contentsJson.append("{\"role\":\"model\",\"parts\":[{\"text\":\"Ciente. Conduzirei as respostas com fundamentação legal e objetividade.\"}]},");

        if (historico != null) {
            for (MensagemDTO msg : historico) {
                contentsJson.append("{\"role\":\"").append(msg.getRole()).append("\",\"parts\":[{\"text\":\"")
                        .append(escaparTexto(msg.getTexto())).append("\"}]},");
            }
        }

        contentsJson.append("{\"role\":\"user\",\"parts\":[{\"text\":\"").append(escaparTexto(mensagemAtual)).append("\"}]}");
        contentsJson.append("]");

        String payload = "{\"contents\":" + contentsJson.toString() + "}";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String respostaTexto = extrairTextoResposta(response.body());
                verificarEPersistirAcordo(respostaTexto, est, cacheMinimoPiso);
                return respostaTexto;
            } else {
                return "[ERRO HTTP " + response.statusCode() + "]: " + response.body();
            }
        } catch (Exception e) {
            return "[ERRO SERVIÇO]: " + e.getMessage();
        }
    }

    private void verificarEPersistirAcordo(String respostaIa, Estabelecimento est, double valorFechado) {
        if (respostaIa.toLowerCase().contains("negócio fechado")) {
            if (contratoRepository == null) {
                System.out.println("[AVISO]: Executando fora do contexto Spring. Repositório H2 não injetado.");
                return;
            }
            try {
                ContratoEntity entidade = new ContratoEntity(est.getNome(), "Agente IA", valorFechado);
                contratoRepository.save(entidade);
                System.out.println("[BANCO H2]: Contrato registrado na tabela tb_contratos com sucesso!");
            } catch (Exception e) {
                System.err.println("[ERRO PERSISTÊNCIA H2]: " + e.getMessage());
            }
        }
    }

    private String escaparTexto(String texto) {
        if (texto == null) return "";
        return texto.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    private String extrairTextoResposta(String json) {
        int chaveIndex = json.indexOf("\"text\":");
        if (chaveIndex != -1) {
            int inicio = json.indexOf("\"", chaveIndex + 7) + 1;
            int fim = json.indexOf("\"", inicio);
            if (inicio > 0 && fim > inicio) {
                return json.substring(inicio, fim).replace("\\n", "\n").replace("\\\"", "\"");
            }
        }
        return json;
    }
}