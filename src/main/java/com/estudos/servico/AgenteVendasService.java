package com.estudos.servico;

import java.util.List;
import java.util.Locale;

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
import com.estudos.modelo.Formacao;
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

        Formacao formacao = Formacao.fromQuantidadeMusicos(quantidadeMusicos);
        if (formacao == null) {
            return "[RECUSADO]: As formações permitidas são Solo (1), Duo (2) e Trio (3).";
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
                "1. Formações permitidas: Solo (1), Duo (2) e Trio (3) músicos.\n" +
                "2. O cachê bruto mínimo informado como referência sindical do SINDIMUCE é R$ 400 por músico para apresentações de até 3 horas: Solo R$ 400, Duo R$ 800 e Trio R$ 1.200.\n" +
                "3. Nunca recomende, aceite ou apresente como cachê negociado um valor abaixo do mínimo correspondente à formação. Apresente a penalidade de cancelamento como ajuste separado do cachê bruto.\n" +
                "4. Não invente valores de hora extra. Quando o local não tiver equipamento de som, informe que esse custo é negociado separadamente; se tiver equipamento próprio, não inclua aluguel de som.\n" +
                "5. Use técnicas de venda consultiva para ajudar as partes a esclarecer valor, data, duração, repertório e condições, sem afirmar que um contrato foi fechado antes da confirmação das partes.\n" +
                "6. Avalie o perfil do usuário e a mensagem para responder de forma coerente (se artista ou contratante).\n\n" +
                "DADOS DO USUÁRIO CONECTADO:\n" +
                "- Nome: %s\n" +
                "- Estabelecimento: %s\n" +
                "- Endereço: %s\n" +
                "- Gênero Musical: %s\n" +
                "- Formação: %s (%d músicos), cachê mínimo de referência R$ %.2f\n\n" +
                "Referências informadas: tabela de cachês do SINDIMUCE; CLT e Lei Federal nº 3.857/1960 são referências legais, sem afirmar que fixam estes valores. O piso não foi validado contra uma tabela oficial neste momento.\n\n" +
                "Responda diretamente em português de forma clara e objetiva.",
                usuario.getNome() != null ? usuario.getNome() : "Não informado",
                usuario.getEstabelecimento() != null ? usuario.getEstabelecimento() : "Não informado",
                usuario.getEndereco() != null ? usuario.getEndereco() : "Não informado",
                usuario.getGeneroMusical() != null ? usuario.getGeneroMusical() : "Geral",
                formacao.getDescricao(),
                quantidadeMusicos,
                formacao.getCacheMinimo()
            );
            contexto += montarCatalogoPublico(usuario);
        } else {
            contexto = String.format(
                "Você é o agente consultor do Cura. Formações permitidas: Solo (1), Duo (2) e Trio (3). " +
                        "O cachê bruto mínimo informado como referência sindical do SINDIMUCE é R$ 400 por músico para apresentações de até 3 horas. " +
                        "Para a formação %s (%d músicos), o mínimo é R$ %.2f. Nunca recomende nem aceite cachê abaixo desse mínimo. " +
                        "Não invente valores de hora extra. Som sem equipamento é negociado separadamente; não cobre aluguel se o local já possui som. " +
                        "Use venda consultiva, sem dizer que o contrato está fechado antes do aceite das partes. Responda em português.",
                formacao.getDescricao(),
                quantidadeMusicos,
                formacao.getCacheMinimo()
            );
        }

        return executarChamadaOpenRouter(contexto, mensagem, historico);
    }

    public String processarAtendimento(Estabelecimento estabelecimento, String mensagem, List<MensagemDTO> historico, int quantidadeMusicos) {
        Formacao formacao = Formacao.fromQuantidadeMusicos(quantidadeMusicos);
        if (formacao == null) {
            return "[RECUSADO]: As formações permitidas são Solo (1), Duo (2) e Trio (3).";
        }
        String contexto = String.format(
                "Você é o agente consultor do Cura. Formações: Solo, Duo e Trio. " +
                        "O cachê bruto mínimo informado como referência sindical é R$ 400 por músico para apresentações de até 3 horas. " +
                        "A formação atual é %s (%d músicos), mínimo R$ %.2f. Nunca recomende nem aceite abaixo do mínimo. " +
                        "Som sem equipamento é negociado separadamente; sem aluguel se o local já possui equipamento. " +
                        "Não invente valores de hora extra e não afirme fechamento sem aceite de ambas as partes. Responda em português.",
                formacao.getDescricao(),
                quantidadeMusicos,
                formacao.getCacheMinimo()
        );
        if (estabelecimento != null) {
            contexto += String.format(
                    " O cliente é '%s', localizado em '%s'. %s",
                    estabelecimento.getNome() != null ? estabelecimento.getNome() : "Não informado",
                    estabelecimento.getEndereco() != null ? estabelecimento.getEndereco() : "Não informado",
                    estabelecimento.isPossuiEquipamentoSom()
                            ? "O estabelecimento possui equipamento de som; não inclua aluguel de som."
                            : "O estabelecimento não possui equipamento de som; aluguel/serviço de som deve ser negociado separadamente."
            );
        }
        return executarChamadaOpenRouter(contexto, mensagem, historico);
    }

    public String processarAtendimento(Estabelecimento estabelecimento, String mensagem) {
        return processarAtendimento(estabelecimento, mensagem, null, 1);
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

    private String montarCatalogoPublico(UsuarioEntity usuario) {
        String perfilAlvo = "ARTISTA".equals(usuario.getTipoPerfil()) ? "ESTABELECIMENTO" : "ARTISTA";
        List<UsuarioEntity> perfis = usuarioRepository.findByTipoPerfilOrderByNomeAsc(perfilAlvo).stream()
                .filter(perfil -> perfil.getId() != null && !perfil.getId().equals(usuario.getId()))
                .limit(20)
                .toList();
        if (perfis.isEmpty()) {
            return "\n\nNão há perfis correspondentes registrados no catálogo neste momento.";
        }

        StringBuilder catalogo = new StringBuilder(
                "\n\nPERFIS PÚBLICOS DO CATÁLOGO (não revele e-mail, CPF/CNPJ ou endereço; contatos só após interesse mútuo):");
        for (UsuarioEntity perfil : perfis) {
            catalogo.append("\n- ").append(perfil.getNome());
            if (perfil.getCidade() != null) {
                catalogo.append(" | Cidade: ").append(perfil.getCidade());
            }
            if ("ARTISTA".equals(perfilAlvo)) {
                if (perfil.getGeneroMusical() != null) {
                    catalogo.append(" | Gênero: ").append(perfil.getGeneroMusical());
                }
                Formacao formacaoPerfil = perfil.getQuantidadeMusicos() == null
                        ? null
                        : Formacao.fromQuantidadeMusicos(perfil.getQuantidadeMusicos());
                if (formacaoPerfil != null) {
                    catalogo.append(" | Formação: ").append(formacaoPerfil.getDescricao())
                            .append(" | Cachê mínimo: R$ ")
                            .append(String.format(Locale.forLanguageTag("pt-BR"), "%.2f", formacaoPerfil.getCacheMinimo()));
                }
            } else {
                if (perfil.getTipoEstabelecimento() != null) {
                    catalogo.append(" | Tipo: ").append(perfil.getTipoEstabelecimento());
                }
                if (perfil.getGeneroDesejado() != null) {
                    catalogo.append(" | Gêneros desejados: ").append(perfil.getGeneroDesejado());
                }
                if (perfil.getPossuiEquipamentoSom() != null) {
                    catalogo.append(perfil.getPossuiEquipamentoSom()
                            ? " | Possui equipamento de som"
                            : " | Não possui equipamento de som; locação deve ser negociada separadamente");
                }
            }
            if (perfil.getDescricao() != null) {
                catalogo.append(" | Sobre: ").append(perfil.getDescricao());
            }
        }
        catalogo.append("\nApresente opções compatíveis como sugestões e explique que o usuário deve demonstrar interesse para iniciar contato.");
        return catalogo.toString();
    }
}