package com.estudos.persistencia;

import com.estudos.modelo.Contrato;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ContratoDAO {
    private static final String CAMINHO_ARQUIVO = "contratos_registrados.txt";

    public void salvar(Contrato contrato) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CAMINHO_ARQUIVO, true))) {
            String registro = String.format("CONTRATO | Estabelecimento: %s | Artista: %s | Valor: R$ %.2f",
                    contrato.getProposta().getEstabelecimento().getNome(),
                    contrato.getProposta().getArtista().getNome(),
                    contrato.getProposta().getArtista().getValorShow());

            writer.write(registro);
            writer.newLine();
            System.out.println("  [PERSISTÊNCIA] Contrato salvo em " + CAMINHO_ARQUIVO);
        } catch (IOException e) {
            System.err.println("  [ERRO PERSISTÊNCIA] Falha ao salvar contrato: " + e.getMessage());
        }
    }

    public void listar() {
        System.out.println("\n=== HISTÓRICO DE CONTRATOS REGISTRADOS ===");
        try (BufferedReader reader = new BufferedReader(new FileReader(CAMINHO_ARQUIVO))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                System.out.println(linha);
            }
        } catch (IOException e) {
            System.out.println("Nenhum histórico encontrado ou falha na leitura: " + e.getMessage());
        }
    }
}