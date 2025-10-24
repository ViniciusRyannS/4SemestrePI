package br.com.julio.pi.backoffice_users.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class CepService {
    private static final Logger log = LoggerFactory.getLogger(CepService.class);
    private final RestTemplate http = new RestTemplate();

    public record CepDTO(String cep, String logradouro, String bairro, String localidade, String uf) {}

    public Optional<CepDTO> consultar(String cep) {
        try {
            String url = "https://viacep.com.br/ws/" + cep + "/json";
            ResponseEntity<CepDTO> res = http.getForEntity(url, CepDTO.class);
            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                return Optional.of(res.getBody());
            }
            log.warn("ViaCEP retornou status {}", res.getStatusCode());
            return Optional.empty();
        } catch (RestClientException e) {
            log.warn("Falha na consulta CEP {}: {}", cep, e.getMessage());
            return Optional.empty(); // NUNCA propaga erro
        }
    }
}
