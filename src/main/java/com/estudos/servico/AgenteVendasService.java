package com.estudos.servico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.estudos.dto.MensagemDTO;
import com.estudos.modelo.Estabelecimento;

@Service
public class AgenteVendasService {

    private final String apiKey = System.getenv("GEMINI_API_KEY");
    private final String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key="
            + apiKey;
    private final RestTemplate restTemplate = new RestTemplate();

    // --- MÉTODOS DE SOBRECARGA (COMPATIBILIDADE RETROATIVA) ---

    // Compatibilidade para chamadas legadas com 2 parâmetros
    public String processarAtendimento(Estabelecimento est, String mensagem) {
        return processarAtendimento(est, mensagem, Collections.emptyList(), 1, Collections.emptyList());
    }

    // Compatibilidade para chamadas legadas com 4 parâmetros
    public String processarAtendimento(Estabelecimento est, String mensagem, List<MensagemDTO> historico,
            int qtdMusicos) {
        return processarAtendimento(est, mensagem, historico, qtdMusicos, Collections.emptyList());
    }

    // --- MÉTODO PRINCIPAL (5 PARÂMETROS) ---
    public String processarAtendimento(
            Estabelecimento est,
            String mensagemAtual,
            List<MensagemDTO> historico,
            int qtdMusicos,
            List<String> artistasCadastrados) {

        String listaArtistasStr = (artistasCadastrados == null || artistasCadastrados.isEmpty()) 
                ? "Nenhum artista cadastrado na base no momento." 
                : String.join(", ", artistasCadastrados);

        int totalIntegrantes = Math.max(qtdMusicos, 1);
        double cacheMinimoPiso = totalIntegrantes * 420.0;

        String contextoComercial = String.format(
            "Você é um agente de vendas e consultor artístico comercial para apresentações ao vivo no Ceará.\n" +
            "Seu papel é negociar contratos de forma consultiva, pragmática, concisa e orientada a soluções.\n\n" +
            "DADOS DA NEGOCIAÇÃO E PLATAFORMA:\n" +
            "- Contratante: %s (%s)\n" +
            "- Endereço: %s\n" +
            "- Formação Atual Solicitada: %d músico(s)\n" +
            "- Estilo Base: %s\n" +
            "- Estrutura de Som: %b\n" +
            "- ARTISTAS DISPONÍVEIS NA BASE (H2): %s\n" +
            "- Piso Mínimo Inegociável (SINDIMUCE / Lei Estadual nº 15.112/2012): R$ %.2f (R$ 420,00 por músico).\n\n" +
            "DIRETRIZES DE NEGOCIAÇÃO E TRATAMENTO DE OBJEÇÕES:\n" +
            "1. PISO INVIOLÁVEL: Nunca aceite valores abaixo de R$ %.2f para a formação solicitada. Recuse citando a Lei Estadual nº 15.112/2012 e a tabela do SINDIMUCE. Se o contratante tiver orçamento menor, sugira reduzir o formato e pergunte se ele aceita.\n" +
            "2. RECOMENDAÇÃO DE ARTISTAS: Quando solicitado indicação de nomes, cite prioritariamente os artistas disponíveis na base informada acima.\n" +
            "3. CONTRAPROPOSTAS E PERGUNTAS: Ao sugerir adaptação de repertório, redução de músicos ou nova data, finalize APENAS com a pergunta de validação. É ESTRITAMENTE PROIBIDO usar o termo 'Negócio fechado!' enquanto aguarda a confirmação do contratante.\n" +
            "4. FECHAMENTO DEFINITIVO: Use a expressão exata 'Negócio fechado!' SOMENTE quando o contratante concordar expressamente (ex: 'sim', 'aceito', 'fechado') ou fizer uma oferta direta igual ou superior ao piso. Após a frase, resuma brevemente: Formação, Valor final e Endereço.",
            est.getNome(), est.getTipo(), est.getEndereco(), totalIntegrantes, est.getEstiloMusicalDesejado(), est.isPossuiEquipamentoSom(),
            listaArtistasStr, cacheMinimoPiso, cacheMinimoPiso
        );

        try {
            Map<String, Object> requestBody = new HashMap<>();

            Map<String, Object> systemInstruction = new HashMap<>();
            systemInstruction.put("parts", List.of(Map.of("text", contextoComercial)));
            requestBody.put("system_instruction", systemInstruction);

            List<Map<String, Object>> contents = new ArrayList<>();

            if (historico != null) {
                for (MensagemDTO msg : historico) {
                    Map<String, Object> contentNode = new HashMap<>();
                    contentNode.put("role", msg.getRole());
                    contentNode.put("parts", List.of(Map.of("text", msg.getTexto())));
                    contents.add(contentNode);
                }
            }

            Map<String, Object> mensagemAtualNode = new HashMap<>();
            mensagemAtualNode.put("role", "user");
            mensagemAtualNode.put("parts", List.of(Map.of("text", mensagemAtual)));
            contents.add(mensagemAtualNode);

            requestBody.put("contents", contents);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    return (String) parts.get(0).get("text");
                }
            }

            return "Não foi possível gerar a resposta no momento.";

        } catch (Exception e) {
            System.err.println("Erro na comunicação com a API do Gemini: " + e.getMessage());
            return "Erro temporário ao processar negociação com o agente.";
        }
    }
}