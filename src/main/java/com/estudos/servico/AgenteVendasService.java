package com.estudos.servico;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.estudos.dto.AtendimentoRequestDTO;
import com.estudos.dto.MensagemDTO;
import com.estudos.entidade.UsuarioEntity;
import com.estudos.modelo.Estabelecimento;
import com.estudos.repositorio.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class AgenteVendasService {

    @Value("${openrouter.api.key:}")
    private String openRouterApiKey;

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
        return processarAtendimentoPorUsuario(dto.getUsuarioId(), dto.getMensagem(), dto.getHistorico(), dto.getQuantidadeMusicos());
    }

    public String processarAtendimentoPorUsuario(Long usuarioId, String mensagem, List<MensagemDTO> historico, int quantidadeMusicos) {
        if (usuarioId == null) {
            return "[ERRO]: Usuário não identificado para iniciar o atendimento.";
        }

        if (quantidadeMusicos < 1 || quantidadeMusicos > 5) {
            return "[RECUSADO]: A plataforma aceita apenas formações Solo (1), Duo (2), Trio (3), Quarteto (4) e Quinteto (5). A quantidade informada (" + quantidadeMusicos + ") não é permitida.";
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
                "Você é o agente consultor do Cura.\n\n" +
                "REGRAS OBRIGATÓRIAS DE NEGÓCIO:\n" +
                "1. Formações permitidas: Solo (1), Duo (2), Trio (3), Quarteto (4) e Quinteto (5 músicos).\n" +
                "2. Avalie o perfil do usuário e a mensagem para responder de forma coerente (se artista ou contratante).\n\n" +
                "DADOS DO USUÁRIO CONECTADO:\n" +
                "- Nome: %s\n" +
                "- Estabelecimento: %s\n" +
                "- Endereço: %s\n" +
                "- Gênero Musical: %s\n" +
                "- Músicos na Formação: %d\n\n" +
                "Responda diretamente em português de forma clara e objetiva.",
                usuario.getNome() != null ? usuario.getNome() : "Não informado",
                usuario.getEstabelecimento() != null ? usuario.getEstabelecimento() : "Não informado",
                usuario.getEndereco() != null ? usuario.getEndereco() : "Não informado",
                usuario.getGeneroMusical() != null ? usuario.getGeneroMusical() : "Geral",
                quantidadeMusicos
            );
        } else {
            contexto = String.format(
                "Você é o agente consultor do Cura. Formações permitidas: de 1 a 5 músicos. Músicos informados: %d. Responda em português.",
                quantidadeMusicos
            );
        }

        return executarChamadaOpenRouter(contexto, mensagem, historico);
    }

    public String processarAtendimento(Estabelecimento estabelecimento, String mensagem, List<MensagemDTO> historico, int quantidadeMusicos) {
        if (quantidadeMusicos < 1 || quantidadeMusicos > 5) {
            return "[RECUSADO]: A plataforma aceita apenas formações Solo (1), Duo (2), Trio (3), Quarteto (4) e Quinteto (5). A quantidade informada (" + quantidadeMusicos + ") não é permitida.";
        }
        return processarAtendimento(estabelecimento, mensagem);
    }

    public String processarAtendimento(Estabelecimento estabelecimento, String mensagem) {
        String contexto = "Você é o agente consultor do Cura. Responda em português de forma objetiva.";
        if (estabelecimento != null) {
            contexto = String.format(
                "Você é o agente consultor do Cura. O cliente é '%s', localizado em '%s'. Responda em português.",
                estabelecimento.getNome() != null ? estabelecimento.getNome() : "Não informado",
                estabelecimento.getEndereco() != null ? estabelecimento.getEndereco() : "Não informado"
            );
        }
        return executarChamadaOpenRouter(contexto, mensagem, null);
    }

    private String executarChamadaOpenRouter(String contexto, String mensagem, List<MensagemDTO> historico) {
        String key = (openRouterApiKey != null && !openRouterApiKey.trim().isEmpty()) 
            ? openRouterApiKey 
            : System.getenv("OPENROUTER_API_KEY");

        if (key == null || key.trim().isEmpty()) {
            return "[ERRO CONFIGURAÇÃO]: A variável OPENROUTER_API_KEY não foi configurada no servidor.";
        }

        try {
            String url = "https://openrouter.ai/api/v1/chat/completions";

            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put("model", "meta-llama/llama-3.1-8b-instruct");

            ArrayNode messagesNode = rootNode.putArray("messages");

            ObjectNode systemMsg = messagesNode.addObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", contexto);

            if (historico != null) {
                for (MensagemDTO item : historico) {
                    if (item != null) {
                        ObjectNode histMsg = messagesNode.addObject();
                        JsonNode itemNode = objectMapper.valueToTree(item);

                        String role = "user";
                        if (itemNode.hasNonNull("role")) {
                            role = itemNode.get("role").asText();
                        } else if (itemNode.hasNonNull("papel")) {
                            role = itemNode.get("papel").asText();
                        }

                        String content = "";
                        if (itemNode.hasNonNull("content")) {
                            content = itemNode.get("content").asText();
                        } else if (itemNode.hasNonNull("conteudo")) {
                            content = itemNode.get("conteudo").asText();
                        } else if (itemNode.hasNonNull("mensagem")) {
                            content = itemNode.get("mensagem").asText();
                        } else if (itemNode.hasNonNull("texto")) {
                            content = itemNode.get("texto").asText();
                        }

                        histMsg.put("role", role);
                        histMsg.put("content", content);
                    }
                }
            }

            ObjectNode userMsg = messagesNode.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", mensagem);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(key);

            HttpEntity<String> request = new HttpEntity<>(rootNode.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonResposta = objectMapper.readTree(response.getBody());
                return jsonResposta.path("choices").get(0).path("message").path("content").asText();
            }

            return "[ERRO DESCONHECIDO]: Sem resposta do OpenRouter.";

        } catch (HttpStatusCodeException e) {
            System.err.println("=== ERRO DA API OPENROUTER ===");
            System.err.println("Status Code: " + e.getStatusCode());
            System.err.println("Resposta: " + e.getResponseBodyAsString());
            return "[ERRO OPENROUTER " + e.getRawStatusCode() + "]: " + e.getResponseBodyAsString();

        } catch (ResourceAccessException e) {
            System.err.println("=== TIMEOUT NO OPENROUTER ===");
            e.printStackTrace();
            return "[ERRO CONEXÃO]: O servidor OpenRouter demorou para responder.";

        } catch (Exception e) {
            System.err.println("=== EXCEÇÃO INTERNA ===");
            e.printStackTrace();
            return "[ERRO INTERNO]: " + e.getMessage();
        }
    }
}