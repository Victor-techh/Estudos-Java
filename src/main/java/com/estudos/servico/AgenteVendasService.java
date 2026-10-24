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

import com.estudos.agent.AgentPromptProvider;
import com.estudos.agent.AgentPromptProvider.PromptDefinition;
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

    // Injeta a chave do OpenRouter definida em application.properties ou ambiente
    @Value("${openrouter.api.key:}")
    private String openRouterApiKey;

    private final UsuarioRepository usuarioRepository;
    private final AgentPromptProvider agentPromptProvider;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Construtor sem argumentos para manter compatibilidade com instancias diretas (ex: Principal.java)
    public AgenteVendasService() {
        this.usuarioRepository = null;
        this.agentPromptProvider = new AgentPromptProvider();
    }

    public AgenteVendasService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.agentPromptProvider = new AgentPromptProvider();
    }

    // Construtor utilizado pelo Spring para injetar dependencias do banco
    @Autowired
    public AgenteVendasService(UsuarioRepository usuarioRepository, AgentPromptProvider agentPromptProvider) {
        this.usuarioRepository = usuarioRepository;
        this.agentPromptProvider = agentPromptProvider;
    }

    // Ponto de entrada chamado pelo AgenteController processando o DTO da requisição
    public String processarAtendimentoPorUsuario(AtendimentoRequestDTO dto) {
        if (dto == null) {
            return "[ERRO]: Payload inválido.";
        }
        return processarAtendimentoPorUsuario(
            dto.getUsuarioId(),
            dto.getMensagem(),
            dto.getHistorico(),
            dto.getQuantidadeMusicos()
        );
    }

    // Processa a mensagem buscando dados do usuario no banco para montar o contexto
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

        return processarAtendimentoPorUsuario(usuario, mensagem, historico, quantidadeMusicos);
    }

    // Processa a mensagem diretamente a partir da entidade do usuário
    public String processarAtendimentoPorUsuario(UsuarioEntity usuario, String mensagem, List<MensagemDTO> historico, int quantidadeMusicos) {
        if (usuario == null) {
            return "[ERRO]: Usuário não identificado para iniciar o atendimento.";
        }

        // Montagem do prompt de sistema com os dados contextuais do cliente
        String contexto = String.format(
            "Nome: %s. Perfil: %s. Estabelecimento: '%s'. Localização: '%s'. " +
            "Preferência musical: '%s'. Quantidade recebida do formulário (o valor 1 pode ser padrão, não confirmação): %d.",
            (usuario.getNome() != null && !usuario.getNome().isBlank()) ? usuario.getNome() : (usuario.getEmail() != null ? usuario.getEmail().split("@")[0] : "Cliente"),
            normalizarTipoPerfil(usuario.getTipoPerfil()),
            (usuario.getEstabelecimento() != null && !usuario.getEstabelecimento().isBlank()) ? usuario.getEstabelecimento() : "Não informado",
            (usuario.getEndereco() != null && !usuario.getEndereco().isBlank()) ? usuario.getEndereco() : "Não informado",
            (usuario.getGeneroMusical() != null && !usuario.getGeneroMusical().isBlank()) ? usuario.getGeneroMusical() : "Não informado",
            Math.max(1, quantidadeMusicos)
        );

        return executarAtendimentoComContexto(contexto, mensagem, historico);
    }

    private String normalizarTipoPerfil(String tipoPerfil) {
        if (tipoPerfil == null || tipoPerfil.isBlank()) {
            return "Não informado";
        }
        return switch (tipoPerfil.trim().toUpperCase(java.util.Locale.ROOT)) {
            case "ESTABELECIMENTO", "CONTRATANTE" -> "CONTRATANTE";
            case "ARTISTA" -> "ARTISTA";
            default -> tipoPerfil.trim();
        };
    }

    // Sobrecargas para manter compatibilidade com chamadas legadas
    public String processarAtendimento(Estabelecimento estabelecimento, String mensagem, List<MensagemDTO> historico, int quantidadeMusicos) {
        return executarAtendimentoComContexto(criarContextoEstabelecimento(estabelecimento), mensagem, historico);
    }

    public String processarAtendimento(Estabelecimento estabelecimento, String mensagem) {
        return processarAtendimento(estabelecimento, mensagem, null, 1);
    }

    private String criarContextoEstabelecimento(Estabelecimento estabelecimento) {
        String contexto = "";
        if (estabelecimento != null) {
            contexto = String.format(
                "Estabelecimento: '%s'. Localização: '%s'.",
                estabelecimento.getNome() != null ? estabelecimento.getNome() : "Não informado",
                estabelecimento.getEndereco() != null ? estabelecimento.getEndereco() : "Não informado"
            );
        }
        return contexto;
    }

    private String executarAtendimentoComContexto(String contexto, String mensagem, List<MensagemDTO> historico) {
        PromptDefinition prompt = agentPromptProvider.build(contexto);
        String promptCompleto = prompt.systemInstructions() + "\n\n" + prompt.contextSummary();
        return executarChamadaOpenRouter(promptCompleto, mensagem, historico);
    }

    // Executa a integracao HTTP com a API do OpenRouter
    private String executarChamadaOpenRouter(String contexto, String mensagem, List<MensagemDTO> historico) {
        // Fallback para buscar a chave na propriedade Spring ou na variavel de ambiente do Render
        String key = (openRouterApiKey != null && !openRouterApiKey.trim().isEmpty()) 
            ? openRouterApiKey 
            : System.getenv("OPENROUTER_API_KEY");

        if (key == null || key.trim().isEmpty()) {
            return "[ERRO CONFIGURAÇÃO]: A variável OPENROUTER_API_KEY não foi configurada no servidor.";
        }

        try {
            // Endpoint padrao OpenAI compatível com OpenRouter
            String url = "https://openrouter.ai/api/v1/chat/completions";

            // Construção do payload JSON via Jackson para evitar erros de sintaxe manual
            ObjectNode rootNode = objectMapper.createObjectNode();
            
            rootNode.put("model", "meta-llama/llama-3.1-8b-instruct");

            ArrayNode messagesNode = rootNode.putArray("messages");

            // Mensagem de sistema (instruções do agente)
            ObjectNode systemMsg = messagesNode.addObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", contexto);

            adicionarHistorico(messagesNode, historico, mensagem);

            // Mensagem do usuário
            ObjectNode userMsg = messagesNode.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", mensagem);

            // Configuração dos cabecalhos HTTP com autenticação Bearer Token
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(key);

            HttpEntity<String> request = new HttpEntity<>(rootNode.toString(), headers);

            // Envio da requisição POST
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            // Extração do texto de resposta do JSON retornado pela API
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonResposta = objectMapper.readTree(response.getBody());
                return jsonResposta.path("choices").get(0).path("message").path("content").asText();
            }

            return "[ERRO DESCONHECIDO]: Sem resposta do OpenRouter.";

        } catch (HttpStatusCodeException e) {
            // Captura respostas de erro da API (4xx e 5xx) sem derrubar a aplicação
            System.err.println("=== ERRO DA API OPENROUTER ===");
            System.err.println("Status Code: " + e.getStatusCode());
            System.err.println("Resposta: " + e.getResponseBodyAsString());
            return "[ERRO OPENROUTER " + e.getRawStatusCode() + "]: " + e.getResponseBodyAsString();

        } catch (ResourceAccessException e) {
            // Tratamento de falhas de rede e timeouts
            System.err.println("=== TIMEOUT NO OPENROUTER ===");
            e.printStackTrace();
            return "[ERRO CONEXÃO]: O servidor OpenRouter demorou para responder.";

        } catch (Exception e) {
            // Tratamento generico para exceções de parsing ou execução interna
            System.err.println("=== EXCEÇÃO INTERNA ===");
            e.printStackTrace();
            return "[ERRO INTERNO]: " + e.getMessage();
        }
    }

    private void adicionarHistorico(ArrayNode messagesNode, List<MensagemDTO> historico, String mensagemAtual) {
        if (historico == null || historico.isEmpty()) {
            return;
        }

        int ultimoIndice = historico.size() - 1;
        for (int i = 0; i < historico.size(); i++) {
            MensagemDTO mensagem = historico.get(i);
            
            if (mensagem == null || mensagem.getTexto() == null || mensagem.getTexto().isBlank()) {
                continue;
            }

            String role = normalizarRole(mensagem.getRole());
            if (role == null) {
                continue;
            }

            if (i == ultimoIndice && "user".equals(role) && mensagem.getTexto().equals(mensagemAtual)) {
                continue;
            }

            ObjectNode historyMessage = messagesNode.addObject();
            historyMessage.put("role", role);
            historyMessage.put("content", mensagem.getTexto());
        }
    }

    private String normalizarRole(String role) {
        if (role == null) {
            return null;
        }

        return switch (role.trim().toLowerCase()) {
            case "user" -> "user";
            case "assistant", "model" -> "assistant";
            default -> null;
        };
    }
}
