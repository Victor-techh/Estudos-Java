package com.estudos.service;

import com.estudos.dto.AtendimentoRequestDTO;
import com.estudos.model.Usuario;
import com.estudos.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AgenteVendasService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final UsuarioRepository usuarioRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AgenteVendasService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public String processarAtendimentoPorUsuario(AtendimentoRequestDTO dto) {
        if (dto.getUsuarioId() == null) {
            return "[ERRO]: Usuário não identificado para iniciar o atendimento.";
        }

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId()).orElse(null);
        if (usuario == null) {
            return "[ERRO]: Usuário não encontrado no banco de dados.";
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

            String contextoUsuario = String.format(
                "Você é o agente consultor do Cura. O cliente logado é %s, do estabelecimento '%s', localizado em '%s'. " +
                "Gênero musical de preferência: '%s'. Responda diretamente e utilize esses dados no atendimento.",
                usuario.getNome(),
                usuario.getEstabelecimento() != null ? usuario.getEstabelecimento() : "Não informado",
                usuario.getEndereco() != null ? usuario.getEndereco() : "Não informado",
                usuario.getGeneroMusical() != null ? usuario.getGeneroMusical() : "Geral"
            );

            ObjectNode rootNode = objectMapper.createObjectNode();
            ArrayNode contentsNode = rootNode.putArray("contents");

            ObjectNode contentObj = contentsNode.addObject();
            contentObj.put("role", "user");
            ArrayNode partsNode = contentObj.putArray("parts");

            ObjectNode partObj = partsNode.addObject();
            partObj.put("text", contextoUsuario + "\n\nMensagem do cliente: " + dto.getMensagem());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(rootNode.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonResposta = objectMapper.readTree(response.getBody());
                return jsonResposta.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            } else {
                System.err.println("=== ERRO RESPOSTA GEMINI ===");
                System.err.println("Status: " + response.getStatusCode());
                System.err.println("Body: " + response.getBody());
                return "[ERRO SERVIDOR EXTERNO " + response.getStatusCodeValue() + "]: Não foi possível processar a resposta da IA.";
            }

        } catch (Exception e) {
            System.err.println("=== EXCEÇÃO NA CHAMADA DO GEMINI ===");
            e.printStackTrace();
            return "[ERRO TIMEOUT]: Falha ao comunicar com a API do Gemini. Verifique os logs do servidor.";
        }
    }
}
