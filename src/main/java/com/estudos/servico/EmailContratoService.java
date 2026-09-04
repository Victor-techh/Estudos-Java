package com.estudos.servico;

import com.estudos.entidade.ContratoHistoricoEntity;
import com.estudos.repositorio.ContratoHistoricoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Service
public class EmailContratoService {

    private final JavaMailSender mailSender;
    private final ContratoHistoricoRepository repository;

    @Value("${app.contratos.diretorio:./contratos_historico/}")
    private String diretorioContratos;

    public EmailContratoService(JavaMailSender mailSender, ContratoHistoricoRepository repository) {
        this.mailSender = mailSender;
        this.repository = repository;
    }

    public void processarENotificarContrato(String emailCliente, String estabelecimento, String textoContrato) {
        String caminhoArquivo = salvarArquivoLocal(estabelecimento, textoContrato);

        ContratoHistoricoEntity historico = new ContratoHistoricoEntity(
                emailCliente, estabelecimento, textoContrato, caminhoArquivo
        );
        repository.save(historico);

        enviarEmail(emailCliente, "Contrato de Apresentação Artística - Cura", textoContrato);
    }

    private String salvarArquivoLocal(String estabelecimento, String textoContrato) {
        try {
            File pasta = new File(diretorioContratos);
            if (!pasta.exists()) {
                pasta.mkdirs();
            }
            String nomeArquivo = "contrato_" + estabelecimento.replaceAll("\\s+", "_") + "_" + System.currentTimeMillis() + ".txt";
            File arquivo = new File(pasta, nomeArquivo);
            
            try (FileWriter writer = new FileWriter(arquivo)) {
                writer.write(textoContrato);
            }
            return arquivo.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar arquivo de contrato localmente: " + e.getMessage(), e);
        }
    }

    private void enviarEmail(String para, String assunto, String corpo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(para);
        message.setSubject(assunto);
        message.setText(corpo);
        mailSender.send(message);
    }
}