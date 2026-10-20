package com.estudos.servico;

import com.estudos.repositorio.InteresseRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ExpiracaoInteresseService {
    private final InteresseRepository interesseRepository;
    private final int diasRetencao;

    public ExpiracaoInteresseService(
            InteresseRepository interesseRepository,
            @Value("${app.interesses.retencao-dias:7}") int diasRetencao) {
        this.interesseRepository = interesseRepository;
        this.diasRetencao = diasRetencao;
    }

    @Scheduled(fixedDelayString = "${app.interesses.cleanup-interval-ms:3600000}")
    @Transactional
    public void excluirInteressesInativos() {
        interesseRepository.deleteAll(
                interesseRepository.findByAtualizadoEmBefore(LocalDateTime.now().minusDays(diasRetencao))
        );
    }
}
