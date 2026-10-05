package com.estudos.servico;

import com.estudos.dto.AtendimentoRequestDTO;
import com.estudos.dto.MensagemDTO;
import com.estudos.entidade.UsuarioEntity;
import com.estudos.modelo.Estabelecimento;
import com.estudos.repositorio.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class AgenteVendasService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-3.8-flash}")
    private String modelName;

    private final UsuarioRepository usuarioRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AgenteVendasService() {
        this.usuarioRepository = null;
    }

    @Autowired
    public AgenteVendasService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public String processarAtendimentoPorUsuario(AtendimentoRequestDTO dto) {
        if (dto == null) {
            return "[ERRO]: Payload inválido.";
        }
        return processarAtendimentoPorUsuario(dto.getUsuarioId(), dto.getMensagem(), null, dto.getQuantidadeMusicos());
    }

    public String processarAtendimentoPorUsuario(Long usuarioId, String mensagem, List<MensagemDTO> historico, int quantidadeMusicos) {
        if (usuarioId == null) {
            return "[ERRO]: Usuário não identificado para iniciar o atendimento.";
        }

        UsuarioEntity usuario = null;
        if (usuarioRepository != null) {
            usuario = usuarioRepository.findById(usuarioId).orElse(null);
        }

        if (usuario == null && usuarioRepository != null) {
            return "[ERRO]: Usuário não encontrado no banco de dados.";
        }

        String contexto = "";
        if (usuario != null) {
            contexto = String.format(
                "Você é o agente consultor do Cura. O cliente logado é %s, do estabelecimento '%s', localizado em '%s'. " +
                "Gênero musical de preferência: '%s'. Responda diretamente e utilize esses dados no atendimento.",
                usuario.getNome(),
                usuario.getEstabelecimento() != null ? usuario.getEstabelecimento() : "Não informado",
                usuario.getEndereco() != null ? usuario.getEndereco() : "Não informado",
                usuario.getGeneroMusical() != null ? usuario.getGeneroMusical() : "Geral"
            );
        }

        return executarChamadaGemini(contexto, mensagem);
    }

    public String processarAtendimento(Estabelecimento estabelecimento, String mensagem, List<MensagemDTO> historico, int quantidadeMusicos) {
        return processarAtendimento(estabelecimento, mensagem);
    }

    public String processarAtendimento(Estabelecimento estabelecimento, String mensagem) {
        String contexto = "Você é o agente consultor do Cura.";
        if (estabelecimento != null) {
            contexto = String.format(
                "Você é o agente consultor do Cura. O cliente é '%s', localizado em '%s'.",
                estabelecimento.getNome() != null ? estabelecimento.getNome() : "Não informado",
                estabelecimento.getEndereco() != null ? estabelecimento.getEndereco() : "Não informado"
            );
        }
        return executarChamadaGemini(contexto, mensagem);
    }

    private String executarChamadaGemini(String contexto, String mensagem) {
        String key = (apiKey != null && !apiKey.trim().isEmpty()) ? apiKey : System.getenv("GEMINI_API_KEY");
        if (key == null || key.trim().isEmpty()) {
            return "[ERRO CONFIGURAÇÃO]: A variável GEMINI_API_KEY não foi encontrada no servidor.";
        }

        String model = (modelName != null && !modelName.trim().isEmpty()) ? modelName : "gemini-3.8-flash";

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + key;

            ObjectNode rootNode = objectMapper.createObjectNode();
            ArrayNode contentsNode = rootNode.putArray("contents");

            ObjectNode contentObj = contentsNode.addObject();
            contentObj.put("role", "user");
            ArrayNode partsNode = contentObj.putArray("parts");

            ObjectNode partObj = partsNode.addObject();
            partObj.put("text", contexto + "\n\nMensagem do cliente: " + mensagem);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(rootNode.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonResposta = objectMapper.readTree(response.getBody());
                return jsonResposta.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            }

            return "[ERRO DESCONHECIDO]: Resposta sem conteúdo retornada pela IA.";

        } catch (HttpStatusCodeException e) {
            System.err.println("=== ERRO DA API DO GEMINI ===");
            System.err.println("Status Code: " + e.getStatusCode());
            System.err.println("Resposta Google: " + e.getResponseBodyAsString());
            return "[ERRO API " + e.getRawStatusCode() + "]: " + e.getResponseBodyAsString();

        } catch (ResourceAccessException e) {
            System.err.println("=== ERRO DE CONEXÃO/TIMEOUT ===");
            e.printStackTrace();
            return "[ERRO CONEXÃO]: O servidor do Gemini demorou para responder. Tente novamente.";

        } catch (Exception e) {
            System.err.println("=== EXCEÇÃO INTERNA ===");
            e.printStackTrace();
            return "[ERRO INTERNO]: " + e.getMessage();
        }
    }
}
