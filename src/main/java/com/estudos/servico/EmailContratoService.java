package com.estudos.servico;

import com.estudos.entidade.ContratoHistoricoEntity;
import com.estudos.repositorio.ContratoHistoricoRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class EmailContratoService {

    private final ContratoHistoricoRepository repository;
    private final JavaMailSender mailSender;

    public EmailContratoService(ContratoHistoricoRepository repository, JavaMailSender mailSender) {
        this.repository = repository;
        this.mailSender = mailSender;
    }

    public void processarENotificarContrato(String email, String estabelecimento, String textoContrato) {
        // 1. Salva arquivo local
        salvarArquivoLocal(estabelecimento, textoContrato);

        // 2. Persiste no banco de dados H2
        ContratoHistoricoEntity historico = new ContratoHistoricoEntity();
        historico.setEmailDestinatario(email);
        historico.setEstabelecimento(estabelecimento);
        historico.setTextoContrato(textoContrato);
        historico.setDataEnvio(LocalDateTime.now());
        repository.save(historico);

        // 3. Tenta enviar o e-mail sem quebrar a requisição se o Render bloquear a porta SMTP
        try {
            enviarEmail(email, estabelecimento, textoContrato);
        } catch (Exception e) {
            System.err.println("FALHA AO ENVIAR E-MAIL (Bloqueio SMTP/Render): " + e.getMessage());
        }
    }

    private void salvarArquivoLocal(String estabelecimento, String textoContrato) {
        try (FileWriter writer = new FileWriter("contrato_" + System.currentTimeMillis() + ".txt")) {
            writer.write(textoContrato);
        } catch (IOException e) {
            System.err.println("Erro ao salvar arquivo local: " + e.getMessage());
        }
    }

    private void enviarEmail(String email, String estabelecimento, String textoContrato) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Contrato de Prestação de Serviços - " + estabelecimento);
        message.setText(textoContrato);
        mailSender.send(message);
    }
}