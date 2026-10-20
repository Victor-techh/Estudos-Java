package com.estudos.execucao;

import com.estudos.excecao.InfracaoLegalException;
import com.estudos.modelo.*;
import com.estudos.persistencia.ContratoDAO;
import com.estudos.servico.AgenteVendasService;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Principal {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== CADASTRO DO ESTABELECIMENTO ===");
        System.out.print("Nome do estabelecimento: ");
        String nomeEst = scanner.nextLine();

        System.out.println("Selecione o Tipo do Estabelecimento:");
        System.out.println("1 - Bar");
        System.out.println("2 - Casa de Shows");
        System.out.println("3 - Restaurante");
        System.out.println("4 - Pub");
        
        int opcaoTipo = lerInteiroValido(scanner, "Opção (1-4): ");

        String tipoEst;
        switch (opcaoTipo) {
            case 1: tipoEst = "Bar"; break;
            case 2: tipoEst = "Casa de Shows"; break;
            case 3: tipoEst = "Restaurante"; break;
            case 4: tipoEst = "Pub"; break;
            default: tipoEst = "Outro"; break;
        }

        System.out.print("Endereço: ");
        String enderecoEst = scanner.nextLine();

        double orcamentoEst = lerDoubleValido(scanner, "Orçamento máximo (R$): ");

        System.out.print("Estilo musical desejado: ");
        String estiloEst = scanner.nextLine();

        boolean temSom = lerBooleanValido(scanner, "Possui equipamento de som próprio no local? (1 - Sim | 2 - Não): ");

        // Variável declarada no escopo do main
        Estabelecimento estabelecimento = new Estabelecimento(nomeEst, tipoEst, enderecoEst, orcamentoEst, estiloEst, temSom);

        List<Artista> artistas = new ArrayList<>();

        System.out.println("\n=== CADASTRO DE ARTISTAS ===");
        int quantidade = lerInteiroValido(scanner, "Quantos artistas deseja cadastrar para avaliação? ");

        for (int i = 1; i <= quantidade; i++) {
            System.out.println("\n--- Artista " + i + " ---");
            int tipoArtista = lerInteiroValido(scanner, "Formação (1 - Solo | 2 - Duo | 3 - Trio): ");

            System.out.print("Nome: ");
            String nomeArt = scanner.nextLine();

            System.out.print("Estilo Musical: ");
            String estiloArt = scanner.nextLine();

            double valorArt = lerDoubleValido(scanner, "Valor do Show (R$): ");

            if (tipoArtista == Formacao.SOLO.getQuantidadeMusicos()) {
                artistas.add(new ArtistaSolo(nomeArt, estiloArt, valorArt));
            } else if (tipoArtista == Formacao.DUO.getQuantidadeMusicos()
                    || tipoArtista == Formacao.TRIO.getQuantidadeMusicos()) {
                double taxaArt = lerDoubleValido(scanner, "Taxa de Logística (R$): ");
                artistas.add(new Banda(nomeArt, estiloArt, valorArt, taxaArt, tipoArtista));
            } else {
                System.out.println("Formação inválida. Informe 1 (Solo), 2 (Duo) ou 3 (Trio).");
                i--;
            }
        }

        scanner.close();

        ContratoDAO contratoDAO = new ContratoDAO();

        String statusSom = estabelecimento.isPossuiEquipamentoSom() ? "Possui som próprio" : "Não possui som próprio";
        System.out.println("\n--- Processamento de Propostas e Contratos: " + estabelecimento.getNome() + 
                           " (" + estabelecimento.getTipo() + " | " + statusSom + ") ---\n");

        for (Artista artista : artistas) {
            Proposta proposta = new Proposta(artista, estabelecimento);

            if (proposta.isAprovada()) {
                System.out.println("[PROPOSTA APROVADA] " + artista.getNome() + " | Valor Total: R$ " + artista.getValorShow());
                try {
                    Contrato contrato = new Contrato(proposta);
                    System.out.println("  -> Status: CONTRATO EMITIDO COM SUCESSO.");
                    contratoDAO.salvar(contrato);
                } catch (InfracaoLegalException e) {
                    System.out.println("  -> Status: FALHA CONTRATUAL - " + e.getMessage() + "\n");
                } catch (IllegalArgumentException e) {
                    System.out.println("  -> Status: ERRO DE NEGÓCIO - " + e.getMessage() + "\n");
                }
            } else {
                System.out.println("[PROPOSTA RECUSADA] " + artista.getNome() + 
                                   " | Valor Total: R$ " + artista.getValorShow() + 
                                   " | Motivo: " + proposta.getMotivoRecusa() + "\n");
            }
        }

        // Leitura do Histórico
        contratoDAO.listar();

        // Integração do Agente de Vendas IA
        AgenteVendasService agente = new AgenteVendasService();
        System.out.println("\n=== TESTE DO AGENTE DE VENDAS (IA) ===");
        String respostaIA = agente.processarAtendimento(estabelecimento, "Tenho uma banda de Jazz e cobro R$ 1200. Podemos fechar?");
        System.out.println(respostaIA);
    }

    private static int lerInteiroValido(Scanner scanner, String mensagem) {
        while (true) {
            System.out.print(mensagem);
            try {
                int valor = scanner.nextInt();
                scanner.nextLine();
                return valor;
            } catch (InputMismatchException e) {
                System.out.println("  [ERRO ENTRADA DE DADOS] Valor incorreto. Digite apenas números inteiros.");
                scanner.nextLine();
            }
        }
    }

    private static double lerDoubleValido(Scanner scanner, String mensagem) {
        while (true) {
            System.out.print(mensagem);
            try {
                double valor = scanner.nextDouble();
                scanner.nextLine();
                return valor;
            } catch (InputMismatchException e) {
                System.out.println("  [ERRO ENTRADA DE DADOS] Valor incorreto. Digite apenas valores numéricos válidos (ex: 1500.0).");
                scanner.nextLine();
            }
        }
    }

    private static boolean lerBooleanValido(Scanner scanner, String mensagem) {
        while (true) {
            int opcao = lerInteiroValido(scanner, mensagem);
            if (opcao == 1) return true;
            if (opcao == 2) return false;
            System.out.println("  [ERRO ENTRADA DE DADOS] Opção inválida. Digite 1 para Sim ou 2 para Não.");
        }
    }
}