package br.com.julio.pi.backoffice_users.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ViaCepService {
    private final WebClient http = WebClient.create("https://viacep.com.br/ws");

    public ViaCepDTO buscar(String cep) {
        String c = cep == null ? "" : cep.replaceAll("\\D", "");
        if (c.length() != 8) throw new IllegalArgumentException("CEP inválido");
        try {
            return http.get()
                .uri("/{cep}/json", c)
                .retrieve()
                .bodyToMono(ViaCepDTO.class)
                .flatMap(dto -> (dto == null || Boolean.TRUE.equals(dto.erro))
                        ? Mono.error(new IllegalArgumentException("CEP não encontrado"))
                        : Mono.just(dto))
                .block();
        } catch (Exception e) {
            throw new IllegalArgumentException("Falha ao consultar CEP");
        }
    }

    // record simples
    public static record ViaCepDTO(
        String cep, String logradouro, String bairro, String localidade, String uf, Boolean erro
    ) {}
}
