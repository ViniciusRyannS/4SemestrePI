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
            System.out.println(">> [SEED] Iniciando. Total antes = " + repo.count());

            if (!repo.existsByEmail("admin@pi.com")) {
                Usuario u = new Usuario();
                u.setNome("Administrador");
                u.setCpf("00000000000");
                u.setEmail("admin@pi.com");
                String hash = senhaService.gerarHash("admin123"); // sempre hash!
                u.setSenha(hash);
                u.setGrupo(Grupo.ADMINISTRADOR);
                u.setStatus(Status.ATIVO);

                repo.save(u);
                System.out.println(">> [SEED] ADMIN criado (email=admin@pi.com)");
                System.out.println(">> [SEED] HASH salvo = " + hash);
            } else {
                System.out.println(">> [SEED] ADMIN já existe (não vou duplicar).");
            }

            System.out.println(">> [SEED] Concluído. Total depois = " + repo.count());
        };
    }
}
