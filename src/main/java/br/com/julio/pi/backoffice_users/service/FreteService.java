package br.com.julio.pi.backoffice_users.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.com.julio.pi.backoffice_users.frete.FreteOpcao;

@Service
public class FreteService {

    /** Valida CEP (8 dígitos). Retorna só os dígitos. */
    private String validarCep(String cep) {
        if (cep == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CEP é obrigatório");
        String apenasDigitos = cep.replaceAll("\\D", "");
        if (apenasDigitos.length() != 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CEP deve ter 8 dígitos");
        }
        return apenasDigitos;
    }

    /** Regra didática: base + variação pelo último dígito do CEP. */
    public List<FreteOpcao> calcularOpcoes(String cep) {
        String d = validarCep(cep);
        int last = Character.getNumericValue(d.charAt(d.length() - 1));

        BigDecimal eco  = new BigDecimal("19.90").add(BigDecimal.valueOf(last).multiply(new BigDecimal("0.50")));
        BigDecimal rap  = new BigDecimal("29.90").add(BigDecimal.valueOf(last).multiply(new BigDecimal("0.70")));
        BigDecimal expr = new BigDecimal("49.90").add(BigDecimal.valueOf(last).multiply(new BigDecimal("0.90")));

        return List.of(
            new FreteOpcao("Econômico", eco,  "8-12 dias úteis"),
            new FreteOpcao("Rápido",    rap,  "4-6 dias úteis"),
            new FreteOpcao("Expresso",  expr, "1-3 dias úteis")
        );
    }
}
