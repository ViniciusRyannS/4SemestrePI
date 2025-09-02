package br.com.julio.pi.backoffice_users.controller;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.springframework.stereotype.Component;

import br.com.julio.pi.backoffice_users.dto.AlterarSenhaDTO;
import br.com.julio.pi.backoffice_users.dto.UsuarioCreateDTO;
import br.com.julio.pi.backoffice_users.dto.UsuarioUpdateDTO;
import br.com.julio.pi.backoffice_users.model.Usuario;
import br.com.julio.pi.backoffice_users.model.enums.Grupo;
import br.com.julio.pi.backoffice_users.service.UsuarioService;
import br.com.julio.pi.backoffice_users.util.CpfValidator;

@Component
public class ConsoleController {

    private final UsuarioService usuarioService;
    private final Scanner in = new Scanner(System.in);

    public ConsoleController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public void iniciarConsole() {
        while (true) {
            System.out.println();
            System.out.println("=== Backoffice (Sprint 1) ===");
            System.out.print("E-mail: ");
            String email = in.nextLine().trim();
            System.out.print("Senha: ");
            String senha = in.nextLine().trim();

            try {
                Usuario logado = usuarioService.login(email, senha);
                if (logado == null) {
                    System.out.println(">> Credenciais inválidas.");
                    continue;
                }
                System.out.printf("Bem-vindo, %s (%s)%n%n", logado.getNome(), logado.getGrupo());
                menuPrincipal(logado);
            } catch (IllegalStateException e) {
                System.out.println(">> " + e.getMessage());
            } catch (Exception e) {
                System.out.println(">> Erro: " + e.getMessage());
            }
        }
    }

    private void menuPrincipal(Usuario logado) {
        while (true) {
            System.out.println("--- Menu Principal ---");
            if (isAdmin(logado)) {
                System.out.println("1) Listar usuários");
                System.out.println("2) Incluir usuário");
                System.out.println("3) Alterar usuário");
                System.out.println("4) Alterar senha de um usuário");
                System.out.println("5) Ativar/Desativar usuário");
                System.out.println("0) Sair");
            } else {
                System.out.println("1) Listar usuários");
                System.out.println("0) Sair");
            }
            System.out.print("Escolha: ");

            int opc = lerIntSeguro();

            switch (opc) {
                case 1 -> listarUsuarios(logado); 
                case 2 -> {
                    if (!isAdmin(logado)) { deny(); break; }
                    incluirUsuario();
                }
                case 3 -> {
                    if (!isAdmin(logado)) { deny(); break; }
                    alterarUsuario();
                }
                case 4 -> {
                    if (!isAdmin(logado)) { deny(); break; }
                    alterarSenha();
                }
                case 5 -> {
                    if (!isAdmin(logado)) { deny(); break; }
                    toggleStatus();
                }
                case 0 -> {
                    System.out.println("Saindo...");
                    return; 
                }
                default -> System.out.println("Opção inválida!");
            }
            System.out.println(); 
        }
    }

    private void listarUsuarios() {
        List<Usuario> users = usuarioService.findAll();
        System.out.println();
        System.out.printf("%-4s %-20s %-25s %-15s %-8s%n", "ID", "NOME", "EMAIL", "GRUPO", "STATUS");
        for (Usuario u : users) {
            System.out.printf("%-4d %-20s %-25s %-15s %-8s%n",
                    u.getId(), u.getNome(), u.getEmail(), u.getGrupo(), u.getStatus());
        }
    }

    private void listarUsuarios(Usuario logado) {
        listarUsuarios(); 

        if (!isAdmin(logado)) return; 

        System.out.print("\nDigite um ID para ações [0=voltar]: ");
        long id = lerLongSeguro();
        if (id == 0) return;

        submenuUsuario(logado, id);
    }

    private void submenuUsuario(Usuario logado, long id) {
        var opt = usuarioService.findById(id);
        if (opt.isEmpty()) {
            System.out.println(">> Usuário não encontrado.");
            return;
        }
        Usuario u = opt.get();

        System.out.println("\n-- Usuário selecionado --");
        printUsuario(u);

        System.out.println("\nAções:");
        System.out.println("1) Alterar usuário");
        System.out.println("2) Alterar senha");
        System.out.println("3) Ativar/Desativar");
        System.out.println("0) Voltar");
        System.out.print("Escolha: ");
        int op = lerIntSeguro();

        switch (op) {
            case 1 -> alterarUsuarioPorId(id);
            case 2 -> alterarSenhaPorId(id);
            case 3 -> toggleStatusPorId(id);
            default -> { /* volta */ }
        }
    }

    private void incluirUsuario() {
        System.out.println();
        System.out.println("--- Incluir Usuário ---");
        UsuarioCreateDTO dto = new UsuarioCreateDTO();

        System.out.print("Nome: ");
        dto.setNome(in.nextLine().trim());

        System.out.print("CPF (000.000.000-00 ou só números): ");
        String cpf = in.nextLine().trim();
        if (!CpfValidator.isValid(cpf)) {
            System.out.println(">> CPF inválido.");
            return;
        }
        dto.setCpf(cpf);

        System.out.print("E-mail: ");
        dto.setEmail(in.nextLine().trim());
        if (!emailValido(dto.getEmail())) {
            System.out.println(">> E-mail inválido.");
            return;
        }

        System.out.print("Senha: ");
        dto.setSenha(in.nextLine().trim());
        System.out.print("Confirmar senha: ");
        String conf = in.nextLine().trim();
        if (!dto.getSenha().equals(conf)) {
            System.out.println(">> Senhas não conferem.");
            return;
        }

        System.out.print("Grupo [1-ADMINISTRADOR | 2-ESTOQUISTA]: ");
        int g = lerIntSeguro();
        dto.setGrupo(g == 1 ? Grupo.ADMINISTRADOR : Grupo.ESTOQUISTA);

        if (!confirmar("Confirmar inclusão?")) return;

        try {
            Usuario novo = usuarioService.create(dto);
            System.out.println(">> Usuário criado com ID " + novo.getId());
        } catch (Exception e) {
            System.out.println(">> Erro ao incluir: " + e.getMessage());
        }
    }

    private void alterarUsuario() {
        System.out.println();
        System.out.println("--- Alterar Usuário ---");
        System.out.print("ID do usuário: ");
        long id = lerLongSeguro();
        alterarUsuarioPorId(id);
    }

    private void alterarUsuarioPorId(long id) {
        Optional<Usuario> opt = usuarioService.findById(id);
        if (opt.isEmpty()) {
            System.out.println(">> Usuário não encontrado.");
            return;
        }

        Usuario existente = opt.get();
        printUsuario(existente);

        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();

        System.out.print("Novo nome: ");
        dto.setNome(in.nextLine().trim());

        System.out.print("Novo CPF (000.000.000-00): ");
        String cpf = in.nextLine().trim();
        if (!CpfValidator.isValid(cpf)) {
            System.out.println(">> CPF inválido.");
            return;
        }
        dto.setCpf(cpf);

        System.out.print("Grupo [1-ADMINISTRADOR | 2-ESTOQUISTA]: ");
        int g = lerIntSeguro();
        dto.setGrupo(g == 1 ? Grupo.ADMINISTRADOR : Grupo.ESTOQUISTA);

        if (!confirmar("Confirmar alterações?")) return;

        try {
            Usuario upd = usuarioService.update(id, dto);
            System.out.println(">> Usuário atualizado: " + upd.getNome());
        } catch (Exception e) {
            System.out.println(">> Erro ao alterar: " + e.getMessage());
        }
    }

    private void alterarSenha() {
        System.out.println();
        System.out.println("--- Alterar Senha ---");
        System.out.print("ID do usuário: ");
        long id = lerLongSeguro();
        alterarSenhaPorId(id);
    }

    
    private void alterarSenhaPorId(long id) {
        Optional<Usuario> opt = usuarioService.findById(id);
        if (opt.isEmpty()) {
            System.out.println(">> Usuário não encontrado.");
            return;
        }
        Usuario u = opt.get();
        printUsuario(u);

        AlterarSenhaDTO dto = new AlterarSenhaDTO();
        System.out.print("Nova senha: ");
        dto.setNovaSenha(in.nextLine().trim());
        System.out.print("Confirmar nova senha: ");
        dto.setConfirmarSenha(in.nextLine().trim());

        if (!dto.getNovaSenha().equals(dto.getConfirmarSenha())) {
            System.out.println(">> Senhas não conferem.");
            return;
        }

        if (!confirmar("Confirmar alteração de senha?")) return;

        try {
            usuarioService.alterarSenha(id, dto.getNovaSenha());
            System.out.println(">> Senha alterada com sucesso.");
        } catch (Exception e) {
            System.out.println(">> Erro ao alterar senha: " + e.getMessage());
        }
    }

    private void toggleStatus() {
        System.out.println();
        System.out.println("--- Ativar/Desativar Usuário ---");
        System.out.print("ID do usuário: ");
        long id = lerLongSeguro();
        toggleStatusPorId(id);
    }

    // usado pelo submenu (e pelo fluxo acima)
    private void toggleStatusPorId(long id) {
        Optional<Usuario> opt = usuarioService.findById(id);
        if (opt.isEmpty()) {
            System.out.println(">> Usuário não encontrado.");
            return;
        }
        Usuario u = opt.get();
        printUsuario(u);

        String pergunta = u.getStatus().name().equals("ATIVO")
                ? "Usuário está ATIVO. Deseja DESATIVAR?"
                : "Usuário está INATIVO. Deseja ATIVAR?";

        if (!confirmar(pergunta)) return;

        try {
            Usuario atualizado = usuarioService.toggleStatus(id);
            System.out.println(">> Novo status de " + atualizado.getNome() + ": " + atualizado.getStatus());
        } catch (Exception e) {
            System.out.println(">> Erro ao atualizar status: " + e.getMessage());
        }
    }

    /* =========================
       HELPERS
       ========================= */

    private boolean isAdmin(Usuario u) {
        return u.getGrupo() == Grupo.ADMINISTRADOR;
    }

    private void deny() {
        System.out.println("Acesso negado: apenas administradores.");
    }

    private boolean emailValido(String e) {
        return e != null && e.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    private boolean confirmar(String pergunta) {
        System.out.print(pergunta + " (S/N): ");
        String r = in.nextLine().trim().toUpperCase();
        return r.startsWith("S");
    }

    private void printUsuario(Usuario u) {
        System.out.printf(
            "ID: %d | Nome: %s | Email: %s | CPF: %s | Grupo: %s | Status: %s%n",
            u.getId(), u.getNome(), u.getEmail(), u.getCpf(), u.getGrupo(), u.getStatus()
        );
    }

    private int lerIntSeguro() {
        while (true) {
            try {
                String s = in.nextLine().trim();
                return Integer.parseInt(s);
            } catch (NumberFormatException | InputMismatchException e) {
                System.out.print("Informe um número válido: ");
            }
        }
    }

    private long lerLongSeguro() {
        while (true) {
            try {
                String s = in.nextLine().trim();
                return Long.parseLong(s);
            } catch (NumberFormatException | InputMismatchException e) {
                System.out.print("Informe um número válido: ");
            }
        }
    }
}
