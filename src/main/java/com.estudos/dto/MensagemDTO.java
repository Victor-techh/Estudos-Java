// DTO que representa cada item individual do historico de conversas
package com.estudos.dto;

public class MensagemDTO {
    private String role;  // "user" para o cliente, "model" para a IA
    private String texto; // Conteudo da mensagem

    public MensagemDTO() {}

    public MensagemDTO(String role, String texto) {
        this.role = role;
        this.texto = texto;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
}