package com.estudos.servico;

import com.estudos.dto.CancelamentoRequest;
import com.estudos.dto.CancelamentoResponse;
import com.estudos.dto.ContratoRequest;
import com.estudos.entidade.CancelamentoContratoEntity;
import com.estudos.entidade.ContratoEntity;
import com.estudos.entidade.InteresseEntity;
import com.estudos.entidade.PenalidadeCancelamentoEntity;
import com.estudos.entidade.UsuarioEntity;
import com.estudos.modelo.Formacao;
import com.estudos.repositorio.CancelamentoContratoRepository;
import com.estudos.repositorio.ContratoRepository;
import com.estudos.repositorio.InteresseRepository;
import com.estudos.repositorio.PenalidadeCancelamentoRepository;
import com.estudos.repositorio.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

@Service
public class ContratoService {
    private static final String INTERESSE_MUTUO = "INTERESSE_MUTUO";
    private static final String ACEITO = "ACEITO";
    private static final String CANCELADO = "CANCELADO";
    private static final double VALOR_CREDITO_ARTISTA = 100.0;

    private final ContratoRepository contratoRepository;
    private final InteresseRepository interesseRepository;
    private final UsuarioRepository usuarioRepository;
    private final PenalidadeCancelamentoRepository penalidadeRepository;
    private final CancelamentoContratoRepository cancelamentoRepository;
    private final GeradorPdfContrato geradorPdfContrato;

    public ContratoService(
            ContratoRepository contratoRepository,
            InteresseRepository interesseRepository,
            UsuarioRepository usuarioRepository,
            PenalidadeCancelamentoRepository penalidadeRepository,
            CancelamentoContratoRepository cancelamentoRepository,
            GeradorPdfContrato geradorPdfContrato) {
        this.contratoRepository = contratoRepository;
        this.interesseRepository = interesseRepository;
        this.usuarioRepository = usuarioRepository;
        this.penalidadeRepository = penalidadeRepository;
        this.cancelamentoRepository = cancelamentoRepository;
        this.geradorPdfContrato = geradorPdfContrato;
    }

    @Transactional
    public ContratoEntity criar(ContratoRequest request, UsuarioEntity atual) {
        validarRequest(request);
        InteresseEntity interesse = interesseRepository.findById(request.interesseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interesse não encontrado."));
        if (!INTERESSE_MUTUO.equals(interesse.getStatus())
                || !interesse.isAceiteArtista()
                || !interesse.isAceiteEstabelecimento()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O contrato exige interesse mútuo confirmado.");
        }
        if (!atual.getId().equals(interesse.getArtistaId())
                && !atual.getId().equals(interesse.getEstabelecimentoId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "O contrato deve pertencer a um interesse da sua conta.");
        }

        UsuarioEntity artista = usuarioRepository.findById(interesse.getArtistaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artista não encontrado."));
        UsuarioEntity estabelecimento = usuarioRepository.findById(interesse.getEstabelecimentoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estabelecimento não encontrado."));
        if (artista.getCpfCnpj() == null || artista.getEndereco() == null || artista.getNomeLegal() == null
                || estabelecimento.getCpfCnpj() == null || estabelecimento.getEndereco() == null
                || estabelecimento.getNomeLegal() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ambas as partes precisam preencher nome civil/razão social, CPF/CNPJ e endereço em /api/auth/dados-contrato.");
        }

        Formacao formacao = Formacao.fromQuantidadeMusicos(request.quantidadeMusicos());
        if (formacao == null || !request.quantidadeMusicos().equals(artista.getQuantidadeMusicos())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A formação do contrato deve ser Solo, Duo ou Trio e corresponder ao perfil do artista.");
        }
        BigDecimal minimo = BigDecimal.valueOf(formacao.getCacheMinimo());
        if (request.cacheBruto().compareTo(minimo) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O cachê bruto não pode ficar abaixo do mínimo de referência de " + minimo + " para essa formação.");
        }

        LocalDate diaApresentacao = request.dataHoraApresentacao().toLocalDate();
        if (request.dataHoraApresentacao().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data da apresentação precisa ser futura.");
        }
        if (request.dataPagamento().isBefore(diaApresentacao)
                || request.dataPagamento().isAfter(diaApresentacao.plusDays(7))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O pagamento deve ocorrer entre a data da apresentação e os 7 dias seguintes.");
        }

        boolean possuiSom = Boolean.TRUE.equals(estabelecimento.getPossuiEquipamentoSom());
        BigDecimal valorSom = request.valorSom() == null ? BigDecimal.ZERO : request.valorSom();
        String condicoesSom = request.condicoesSom() == null ? "" : request.condicoesSom().trim();
        if (valorSom.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O valor de som não pode ser negativo.");
        }
        if (possuiSom && valorSom.signum() > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não se pode cobrar aluguel de som quando o estabelecimento possui equipamento próprio.");
        }
        if (!possuiSom && condicoesSom.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Registre as condições de negociação do som para o local sem equipamento próprio.");
        }

        PenalidadeCancelamentoEntity creditoPendente = penalidadeRepository
                .findFirstByArtistaIdAndAplicadaFalseOrderByCriadaEmAsc(artista.getId())
                .orElse(null);
        double desconto = creditoPendente == null ? 0 : creditoPendente.getValor();
        double valorBruto = request.cacheBruto().doubleValue();

        ContratoEntity contrato = new ContratoEntity();
        contrato.setInteresseId(interesse.getId());
        contrato.setArtistaId(artista.getId());
        contrato.setEstabelecimentoId(estabelecimento.getId());
        contrato.setContratante(nomePublico(estabelecimento));
        contrato.setContratado(artista.getNome());
        contrato.setStatus("AGUARDANDO_ACEITES");
        contrato.setFormacao(formacao.getDescricao());
        contrato.setQuantidadeMusicos(formacao.getQuantidadeMusicos());
        contrato.setDuracaoHoras(request.duracaoHoras());
        contrato.setDataHoraApresentacao(request.dataHoraApresentacao());
        contrato.setDataPagamento(request.dataPagamento());
        contrato.setCpfCnpjContratante(estabelecimento.getCpfCnpj());
        contrato.setNomeLegalContratante(estabelecimento.getNomeLegal());
        contrato.setEnderecoContratante(estabelecimento.getEndereco());
        contrato.setEmailContratante(estabelecimento.getEmail());
        contrato.setCpfCnpjContratado(artista.getCpfCnpj());
        contrato.setNomeLegalContratado(artista.getNomeLegal());
        contrato.setEnderecoContratado(artista.getEndereco());
        contrato.setEmailContratado(artista.getEmail());
        contrato.setValor(valorBruto);
        contrato.setDescontoCancelamento(desconto);
        contrato.setValorLiquidoArtista(valorBruto - desconto);
        contrato.setValorSom(possuiSom ? 0 : valorSom.doubleValue());
        contrato.setCondicoesSom(possuiSom
                ? "Equipamento proprio do estabelecimento; sem aluguel de som."
                : condicoesSom);
        contrato.setEstabelecimentoPossuiSom(possuiSom);

        ContratoEntity salvo = contratoRepository.save(contrato);
        if (creditoPendente != null) {
            creditoPendente.setAplicada(true);
            creditoPendente.setContratoCompensacaoId(salvo.getId());
            penalidadeRepository.save(creditoPendente);
        }
        atualizarPdf(salvo);
        return contratoRepository.save(salvo);
    }

    @Transactional
    public ContratoEntity aceitar(Long contratoId, UsuarioEntity atual) {
        ContratoEntity contrato = carregarContrato(contratoId);
        if (CANCELADO.equals(contrato.getStatus())) {
            throw new ResponseStatusException(HttpStatus.GONE, "O contrato foi cancelado.");
        }
        if (atual.getId().equals(contrato.getArtistaId())) {
            if (contrato.getAceiteArtistaEm() == null) {
                contrato.setAceiteArtistaEm(LocalDateTime.now());
            }
        } else if (atual.getId().equals(contrato.getEstabelecimentoId())) {
            if (contrato.getAceiteContratanteEm() == null) {
                contrato.setAceiteContratanteEm(LocalDateTime.now());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente as partes do contrato podem aceitá-lo.");
        }
        if (contrato.getAceiteArtistaEm() != null && contrato.getAceiteContratanteEm() != null) {
            contrato.setStatus(ACEITO);
        }
        atualizarPdf(contrato);
        return contratoRepository.save(contrato);
    }

    @Transactional
    public CancelamentoContratoEntity cancelar(Long contratoId, CancelamentoRequest request, UsuarioEntity atual) {
        ContratoEntity contrato = carregarContrato(contratoId);
        if (!ACEITO.equals(contrato.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Só é possível cancelar um contrato aceito pelas duas partes.");
        }
        if (cancelamentoRepository.findByContratoId(contratoId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este contrato já possui um registro de cancelamento.");
        }
        LocalDateTime agora = LocalDateTime.now();
        if (!contrato.getDataHoraApresentacao().isAfter(agora)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível registrar cancelamento de uma apresentação já iniciada.");
        }
        boolean dentro48Horas = !contrato.getDataHoraApresentacao().isAfter(agora.plusHours(48));
        CancelamentoContratoEntity cancelamento = new CancelamentoContratoEntity();
        cancelamento.setContratoId(contratoId);
        cancelamento.setCanceladoPorUsuarioId(atual.getId());
        cancelamento.setCriadoEm(agora);
        cancelamento.setDentroJanela48Horas(dentro48Horas);

        if (atual.getId().equals(contrato.getArtistaId())) {
            cancelamento.setPerfilCancelador("ARTISTA");
            boolean atestado = request != null && request.possuiAtestadoMedico();
            cancelamento.setAtestadoSolicitado(atestado);
            if (dentro48Horas && atestado) {
                cancelamento.setStatusRevisao("AGUARDANDO_REVISAO");
            } else if (dentro48Horas) {
                criarPenalidadeArtista(cancelamento, contrato);
                cancelamento.setStatusRevisao("MULTA_ARTISTA_REGISTRADA");
            } else {
                cancelamento.setStatusRevisao("SEM_MULTA_FORA_PRAZO");
            }
            contrato.setStatusCancelamento(cancelamento.getStatusRevisao());
        } else if (atual.getId().equals(contrato.getEstabelecimentoId())) {
            cancelamento.setPerfilCancelador("ESTABELECIMENTO");
            cancelamento.setStatusRevisao(dentro48Horas
                    ? "MULTA_CONTRATANTE_REGISTRADA"
                    : "SEM_MULTA_FORA_PRAZO");
            if (dentro48Horas) {
                double multa = contrato.getValor() * 0.5;
                cancelamento.setMultaContratante(multa);
                contrato.setMultaCancelamentoContratante(multa);
            }
            contrato.setStatusCancelamento(cancelamento.getStatusRevisao());
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente as partes do contrato podem cancelá-lo.");
        }

        contrato.setStatus(CANCELADO);
        cancelamentoRepository.save(cancelamento);
        atualizarPdf(contrato);
        contratoRepository.save(contrato);
        return cancelamento;
    }

    @Transactional
    public CancelamentoContratoEntity revisarCancelamento(Long cancelamentoId, boolean aprovada, UsuarioEntity administrador) {
        CancelamentoContratoEntity cancelamento = cancelamentoRepository.findById(cancelamentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de cancelamento não encontrado."));
        if (!"AGUARDANDO_REVISAO".equals(cancelamento.getStatusRevisao())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esse cancelamento não aguarda revisão médica.");
        }
        ContratoEntity contrato = carregarContrato(cancelamento.getContratoId());
        cancelamento.setRevisadoPorUsuarioId(administrador.getId());
        cancelamento.setRevisadoEm(LocalDateTime.now());
        if (aprovada) {
            cancelamento.setStatusRevisao("JUSTIFICATIVA_MEDICA_APROVADA");
        } else {
            criarPenalidadeArtista(cancelamento, contrato);
            cancelamento.setStatusRevisao("JUSTIFICATIVA_MEDICA_REPROVADA");
        }
        contrato.setStatusCancelamento(cancelamento.getStatusRevisao());
        atualizarPdf(contrato);
        contratoRepository.save(contrato);
        return cancelamentoRepository.save(cancelamento);
    }

    public List<CancelamentoContratoEntity> listarAguardandoRevisao() {
        return cancelamentoRepository.findByStatusRevisao("AGUARDANDO_REVISAO");
    }

    public int pontosNegativosAtivos(Long artistaId) {
        return penalidadeRepository.findByArtistaIdAndPontoNegativoExpiraEmAfter(artistaId, LocalDateTime.now()).size();
    }

    public ContratoEntity carregarContrato(Long id) {
        return contratoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato não encontrado."));
    }

    public void verificarParticipante(ContratoEntity contrato, Long usuarioId) {
        if (!usuarioId.equals(contrato.getArtistaId()) && !usuarioId.equals(contrato.getEstabelecimentoId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esse contrato não pertence à sua conta.");
        }
    }

    private void validarRequest(ContratoRequest request) {
        if (request == null || request.interesseId() == null || request.dataHoraApresentacao() == null
                || request.duracaoHoras() == null || request.quantidadeMusicos() == null
                || request.cacheBruto() == null || request.dataPagamento() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Interesse, data, duração, formação, cachê e data de pagamento são obrigatórios.");
        }
        if (request.duracaoHoras() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A duração da apresentação deve ser positiva.");
        }
        if (request.duracaoHoras() > Formacao.DURACAO_BASE_HORAS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Esta versão só permite contratos de até 3 horas; o cachê para duração maior ainda não foi definido.");
        }
    }

    private void criarPenalidadeArtista(CancelamentoContratoEntity cancelamento, ContratoEntity contrato) {
        if (cancelamento.getId() == null) {
            cancelamentoRepository.save(cancelamento);
        }
        PenalidadeCancelamentoEntity penalidade = new PenalidadeCancelamentoEntity();
        penalidade.setArtistaId(contrato.getArtistaId());
        penalidade.setContratoCanceladoId(contrato.getId());
        penalidade.setCancelamentoId(cancelamento.getId());
        penalidade.setValor(VALOR_CREDITO_ARTISTA);
        penalidade.setCriadaEm(LocalDateTime.now());
        penalidade.setPontoNegativoExpiraEm(LocalDateTime.now().plusMonths(3));
        penalidade.setAplicada(false);
        penalidadeRepository.save(penalidade);
    }

    private void atualizarPdf(ContratoEntity contrato) {
        try {
            byte[] minuta = geradorPdfContrato.gerar(contrato);
            if (contrato.getHashMinuta() == null) {
                contrato.setHashMinuta(hash(minuta));
            }
            contrato.setPdf(geradorPdfContrato.gerar(contrato));
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível gerar o PDF do contrato.", exception);
        }
    }

    private String hash(byte[] conteudo) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(conteudo));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não está disponível nesta JVM.", exception);
        }
    }

    private String nomePublico(UsuarioEntity usuario) {
        return usuario.getEstabelecimento() == null || usuario.getEstabelecimento().isBlank()
                ? usuario.getNome()
                : usuario.getEstabelecimento();
    }
}
