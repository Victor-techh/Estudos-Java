package com.estudos.entidade;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_usuarios")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String nomeLegal;

    @Column(unique = true)
    private String email;

    private String senha;

    private String tipoPerfil; // "ARTISTA" ou "ESTABELECIMENTO"

    private String generoMusical;
    private String estabelecimento;
    private String endereco;
    private String cidade;
    private String descricao;
    private Integer quantidadeMusicos;
    private String generoDesejado;
    private Boolean possuiEquipamentoSom;
    private String tipoEstabelecimento;
    private String cpfCnpj;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getNomeLegal() { return nomeLegal; }
    public void setNomeLegal(String nomeLegal) { this.nomeLegal = nomeLegal; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getTipoPerfil() { return tipoPerfil; }
    public void setTipoPerfil(String tipoPerfil) { this.tipoPerfil = tipoPerfil; }

    public String getGeneroMusical() { return generoMusical; }
    public void setGeneroMusical(String generoMusical) { this.generoMusical = generoMusical; }

    public String getEstabelecimento() { return estabelecimento; }
    public void setEstabelecimento(String estabelecimento) { this.estabelecimento = estabelecimento; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Integer getQuantidadeMusicos() { return quantidadeMusicos; }
    public void setQuantidadeMusicos(Integer quantidadeMusicos) { this.quantidadeMusicos = quantidadeMusicos; }

    public String getGeneroDesejado() { return generoDesejado; }
    public void setGeneroDesejado(String generoDesejado) { this.generoDesejado = generoDesejado; }

    public Boolean getPossuiEquipamentoSom() { return possuiEquipamentoSom; }
    public void setPossuiEquipamentoSom(Boolean possuiEquipamentoSom) { this.possuiEquipamentoSom = possuiEquipamentoSom; }

    public String getTipoEstabelecimento() { return tipoEstabelecimento; }
    public void setTipoEstabelecimento(String tipoEstabelecimento) { this.tipoEstabelecimento = tipoEstabelecimento; }

    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }
}