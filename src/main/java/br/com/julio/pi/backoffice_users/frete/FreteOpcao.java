package br.com.julio.pi.backoffice_users.frete;

import java.math.BigDecimal;

public class FreteOpcao {
    private String nome;      
    private BigDecimal valor;  
    private String prazo;     

    public FreteOpcao() {}

    public FreteOpcao(String nome, BigDecimal valor, String prazo) {
        this.nome = nome;
        this.valor = valor;
        this.prazo = prazo;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public String getPrazo() { return prazo; }
    public void setPrazo(String prazo) { this.prazo = prazo; }
}
