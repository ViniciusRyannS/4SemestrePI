package br.com.julio.pi.backoffice_users.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.julio.pi.backoffice_users.frete.FreteOpcao;
import br.com.julio.pi.backoffice_users.service.FreteService;

@RestController
@RequestMapping("/api/frete")
// @CrossOrigin(origins = "*") // descomente se o front estiver em outra origem
public class FreteController {

    private final FreteService freteService;

    public FreteController(FreteService freteService) {
        this.freteService = freteService;
    }

    /** Ex.: POST /api/frete/opcoes?cep=01311000 */
    @PostMapping("/opcoes")
    public List<FreteOpcao> opcoes(@RequestParam String cep) {
        return freteService.calcularOpcoes(cep);
    }
}
