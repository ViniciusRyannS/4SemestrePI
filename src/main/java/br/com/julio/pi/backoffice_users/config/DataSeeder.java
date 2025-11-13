package br.com.julio.pi.backoffice_users.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import br.com.julio.pi.backoffice_users.model.Usuario;
import br.com.julio.pi.backoffice_users.model.enums.Grupo;
import br.com.julio.pi.backoffice_users.model.enums.Status;
import br.com.julio.pi.backoffice_users.repository.UsuarioRepository;
import br.com.julio.pi.backoffice_users.service.SenhaService;

@Configuration
public class DataSeeder {

    @Bean
    @Order(1)
    CommandLineRunner seedUsuarios(UsuarioRepository repo, SenhaService senhaService) {
        return args -> {

            if (!repo.existsByEmail("admin@pi.com")) {
                Usuario u = new Usuario();
                u.setNome("Administrador");
                u.setCpf("00000000000");
                u.setEmail("admin@pi.com");
                u.setSenha(senhaService.gerarHash("admin123"));
                u.setGrupo(Grupo.ADMINISTRADOR);
                u.setStatus(Status.ATIVO);
                repo.save(u);
            }

            if (!repo.existsByEmail("estoquista@pi.com")) {
                Usuario e = new Usuario();
                e.setNome("Estoquista");
                e.setCpf("11111111111");
                e.setEmail("estoquista@pi.com");
                e.setSenha(senhaService.gerarHash("estoque123"));
                e.setGrupo(Grupo.ESTOQUISTA);
                e.setStatus(Status.ATIVO);
                repo.save(e);
            }

            System.out.println("Total de usuários: " + repo.count());
        };
    }
}
