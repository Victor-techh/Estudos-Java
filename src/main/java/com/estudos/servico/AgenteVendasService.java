package com.estudos.servico;

import com.estudos.dto.MensagemDTO;
import com.estudos.entidade.ContratoEntity;
import com.estudos.entidade.UsuarioEntity;
import com.estudos.modelo.Estabelecimento;
import com.estudos.repositorio.ContratoRepository;
import com.estudos.repositorio.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;

@Service
public class AgenteVendasService {

    private final HttpClient httpClient;
    private final ContratoRepository contratoRepository;
    private final UsuarioRepository usuarioRepository;

    private static final double CACHE_BASE_POR_MUSICO = 420.0;

    public AgenteVendasService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.contratoRepository = null;
        this.usuarioRepository = null;
    }

    @Autowired
    public AgenteVendasService(ContratoRepository contratoRepository, UsuarioRepository usuarioRepository) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.contratoRepository = contratoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public String processarAtendimentoPorUsuario(Long usuarioId, String mensagemAtual, List<MensagemDTO> historico, int qtdMusicos) {
        if (usuarioRepository == null) {
            return "[ERRO CONFIGURAÇÃO]: Repositório de usuários não foi injetado pelo Spring.";
        }

        UsuarioEntity usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado no H2 para o ID: " + usuarioId));

        String nome = usuario.getNome() != null ? usuario.getNome() : "Cliente";
        String tipo = usuario.getTipoPerfil() != null ? usuario.getTipoPerfil() : "ESTABELECIMENTO";
        String endereco = usuario.getEndereco() != null ? usuario.getEndereco() : "Não informado";
        double orcamentoPiso = 0.0;
        String estilo = usuario.getGeneroMusical() != null ? usuario.getGeneroMusical() : "Geral";
        boolean possuiSom = true;

        Estabelecimento est = new Estabelecimento(nome, tipo, endereco, orcamentoPiso, estilo, possuiSom);

        return processarAtendimento(est, mensagemAtual, historico, qtdMusicos);
    }

    public String processarAtendimento(Estabelecimento est, String mensagem) {
        return processarAtendimento(est, mensagem, null, 1);
    }

    public String processarAtendimento(Estabelecimento est, String mensagemAtual, List<MensagemDTO> historico, int qtdMusicos) {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return "[ERRO CONFIGURAÇÃO]: Variável de ambiente GEMINI_API_KEY não definida.";
        }

        int totalIntegrantes = qtdMusicos > 0 ? qtdMusicos : 1;
        double cacheMinimoPiso = totalIntegrantes * CACHE_BASE_POR_MUSICO;

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=" + apiKey;

        String contextoComercial = String.format(
            "Você é um agente de vendas e consultor artístico comercial para apresentações ao vivo no Ceará.\n" +
            "Seu papel é negociar contratos de forma consultiva, pragmática, concisa e orientada a soluções, sem enrolação.\n\n" +
            "DADOS DA NEGOCIAÇÃO:\n" +
            "- Contratante: %s (%s)\n" +
            "- Endereço: %s\n" +
            "- Formação Atual Solicitada: %d músico(s)\n" +
            "- Estilo Base: %s\n" +
            "- Estrutura de Som: %b\n" +
            "- Piso Mínimo Inegociável (SINDIMUCE / Lei Estadual nº 15.112/2012): R$ %.2f (R$ 420,00 por músico).\n\n" +
            "DIRETRIZES DE NEGOCIAÇÃO E TRATAMENTO DE OBJEÇÕES:\n" +
            "1. PISO INVIOLÁVEL: Nunca aceite valores abaixo de R$ %.2f para a formação solicitada. Recuse citando a Lei Estadual nº 15.112/2012 e a tabela do SINDIMUCE. Se o contratante tiver orçamento menor, sugira reduzir o formato (ex: Solo por R$ 420,00) e pergunte se ele aceita.\n" +
            "2. CONTRAPROPOSTAS E PERGUNTAS: Ao sugerir adaptação de repertório, redução de músicos ou nova data, finalize APENAS com a pergunta de validação. É ESTRITAMENTE PROIBIDO usar o termo 'Negócio fechado!' enquanto aguarda a confirmação do contratante.\n" +
            "3. OBJEÇÃO DE ESTILO: Ofereça adaptações (jazz lounge, bossa, releituras instrumentais pop/MPB) que atendam ao público da casa sem descaracterizar a qualidade musical.\n" +
            "4. FECHAMENTO DEFINITIVO: Use a expressão exata 'Negócio fechado!' SOMENTE quando o contratante concordar expressamente (ex: 'sim', 'aceito', 'fechado') ou fizer uma oferta direta igual ou superior ao piso. Após a frase, resuma brevemente: Formação, Valor final e Endereço.",
            est.getNome(), est.getTipo(), est.getEndereco(), totalIntegrantes, est.getEstiloMusicalDesejado(), est.isPossuiEquipamentoSom(),
            cacheMinimoPiso, cacheMinimoPiso
        );

        StringBuilder contentsJson = new StringBuilder();
        contentsJson.append("[");
        contentsJson.append("{\"role\":\"user\",\"parts\":[{\"text\":\"").append(escaparTexto(contextoComercial)).append("\"}]},");
        contentsJson.append("{\"role\":\"model\",\"parts\":[{\"text\":\"Entendido. Atuarei de forma consultiva, defenderei o piso legal e só emitirei fechamento após confirmação explícita.\"}]},");

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
                    .timeout(Duration.ofSeconds(45))
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();

            if (status == 200) {
                String respostaTexto = extrairTextoResposta(response.body());
                verificarEPersistirAcordo(respostaTexto, est, cacheMinimoPiso);
                return respostaTexto;
            } else if (status == 429) {
                return "[ERRO 429 - RATE LIMIT]: Limite de requisições por minuto atingido na API do Google. Aguarde alguns segundos antes de reenviar.";
            } else if (status == 400 || status == 403) {
                return "[ERRO " + status + " - AUTENTICAÇÃO/PAYLOAD]: Verifique a chave de API ou o formato dos dados enviados.";
            } else {
                return "[ERRO SERVIDOR EXTERNO " + status + "]: Não foi possível processar a resposta da IA no momento.";
            }

        } catch (HttpTimeoutException e) {
            return "[ERRO TIMEOUT]: A API do Gemini demorou mais de 45 segundos para responder. Tente novamente.";
        } catch (Exception e) {
            return "[ERRO DE CONEXÃO]: Falha ao comunicar com o servidor da IA: " + e.getMessage();
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