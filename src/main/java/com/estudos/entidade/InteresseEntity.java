package com.estudos.entidade;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tb_interesses",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_interesse_artista_estabelecimento",
                columnNames = {"artista_id", "estabelecimento_id"}
        )
)
public class InteresseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "artista_id", nullable = false)
    private Long artistaId;

    @Column(name = "estabelecimento_id", nullable = false)
    private Long estabelecimentoId;

    private boolean aceiteArtista;
    private boolean aceiteEstabelecimento;
    private String status;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    @Lob
    private String historicoJson;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getArtistaId() { return artistaId; }
    public void setArtistaId(Long artistaId) { this.artistaId = artistaId; }
    public Long getEstabelecimentoId() { return estabelecimentoId; }
    public void setEstabelecimentoId(Long estabelecimentoId) { this.estabelecimentoId = estabelecimentoId; }
    public boolean isAceiteArtista() { return aceiteArtista; }
    public void setAceiteArtista(boolean aceiteArtista) { this.aceiteArtista = aceiteArtista; }
    public boolean isAceiteEstabelecimento() { return aceiteEstabelecimento; }
    public void setAceiteEstabelecimento(boolean aceiteEstabelecimento) { this.aceiteEstabelecimento = aceiteEstabelecimento; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
    public String getHistoricoJson() { return historicoJson; }
    public void setHistoricoJson(String historicoJson) { this.historicoJson = historicoJson; }
}
