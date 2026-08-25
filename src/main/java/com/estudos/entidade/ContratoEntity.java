package com.estudos.entidade;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_contratos")
public class ContratoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contratante;
    private String contratado;
    private Double valor;

    // Construtor padrão obrigatório para o JPA/Jackson
    public ContratoEntity() {}

    public ContratoEntity(String contratante, String contratado, Double valor) {
        this.contratante = contratante;
        this.contratado = contratado;
        this.valor = valor;
    }

    // Getters e Setters obrigatórios para serialização JSON
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContratante() { return contratante; }
    public void setContratante(String contratante) { this.contratante = contratante; }

    public String getContratado() { return contratado; }
    public void setContratado(String contratado) { this.contratado = contratado; }

    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }
}