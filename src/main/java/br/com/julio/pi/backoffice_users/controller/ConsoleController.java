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
<<<<<<< HEAD
<<<<<<< HEAD
                System.out.println("3) Alterar usuário");
                System.out.println("4) Alterar senha de um usuário");
                System.out.println("5) Ativar/Desativar usuário");
=======
>>>>>>> 1931bad (corrigido edicao)
=======
>>>>>>> 1931bad (corrigido edicao)
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
<<<<<<< HEAD
<<<<<<< HEAD
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
=======
>>>>>>> 1931bad (corrigido edicao)
=======
>>>>>>> 1931bad (corrigido edicao)
                case 0 -> {
                    System.out.println("Saindo...");
                    return; 
                }
                default -> System.out.println("Opção inválida!");
            }
            System.out.println(); 
        }
    }

<<<<<<< HEAD
<<<<<<< HEAD
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
=======
=======
>>>>>>> 1931bad (corrigido edicao)
    private void listarUsuarios(Usuario logado) {
        List<Usuario> users = usuarioService.findAll();

        System.out.println("Listar Usuário");
        printSeparator();

        System.out.printf("%-3s | %-20s | %-28s | %-8s | %-13s%n",
                "Id", "Nome", "e-mail", "status", "Grupo");
        printSeparator();

        for (Usuario u : users) {
            String id = String.valueOf(u.getId());
            String nome = crop(u.getNome(), 20);
            String email = crop(u.getEmail(), 28);
            String status = u.getStatus().name().toLowerCase();
            String grupo = (u.getGrupo() == Grupo.ADMINISTRADOR ? "Administrador" : "Estoquista");

            System.out.printf("%-3s | %-20s | %-28s | %-8s | %-13s%n",
                    id, nome, email, status, grupo);
        }
        printSeparator();

        String suffix = isAdmin(logado) ? ", 0 para voltar e i para incluir => " : ", 0 para voltar => ";
        System.out.print("Entre com o id para editar/ativar/inativar" + suffix);
        String resp = in.nextLine().trim();

        if (resp.equalsIgnoreCase("0") || resp.isBlank()) {
            return;
        }
        if (resp.equalsIgnoreCase("i")) {
            if (!isAdmin(logado)) { deny(); return; }
            incluirUsuario();
            return;
        }

        try {
            long id = Long.parseLong(resp);
            Optional<Usuario> opt = usuarioService.findById(id);
            if (opt.isEmpty()) {
                System.out.println(">> Usuário não encontrado.");
                return;
            }
            if (!isAdmin(logado)) {
                System.out.println(">> Acesso negado: apenas administradores podem editar.");
                return;
            }
            telaEdicaoUsuario(opt.get()); 
        } catch (NumberFormatException ex) {
            System.out.println(">> Valor inválido.");
        }
    }

    private void telaEdicaoUsuario(Usuario u) {
        while (true) {
            printDetalheUsuario(u);
            System.out.println("Opções");
            System.out.println("  1) Alterar usuário");
            System.out.println("  2) Alterar senha");
            System.out.println("  3) Ativar/Desativar");
            System.out.println("  4) Voltar Listar Usuário");
            System.out.print("\nEntre com a opção (1,2,3,4) => ");

            int op = lerIntSeguro();
            switch (op) {
                case 1 -> {
                    if (alterarUsuario(u.getId())) {
                        u = usuarioService.findById(u.getId()).orElse(u);
                    }
                }
                case 2 -> alterarSenha(u.getId());
                case 3 -> {
                    Usuario toggled = usuarioService.toggleStatus(u.getId());
                    System.out.println(">> Status atualizado para: " + toggled.getStatus().name().toLowerCase());
                    u = toggled;
                }
                case 4 -> { return; } 
                default -> System.out.println("Opção inválida!");
            }
            System.out.println();
        }
    }

    private void printDetalheUsuario(Usuario u) {
        System.out.println();
        System.out.println("Opção de edição de usuário");
        System.out.println();
        System.out.println("Id: " + u.getId());
        System.out.println("Nome: " + u.getNome());
        System.out.println("Cpf: " + maskCpf(u.getCpf()));
        System.out.println("E-mail: " + u.getEmail());
        System.out.println("Status: " + u.getStatus().name().toLowerCase());
        System.out.println("Grupo: " + (u.getGrupo() == Grupo.ADMINISTRADOR ? "Administrador" : "Estoquista"));
        printSeparator();
<<<<<<< HEAD
>>>>>>> 1931bad (corrigido edicao)
=======
>>>>>>> 1931bad (corrigido edicao)
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
<<<<<<< HEAD
<<<<<<< HEAD
        if (!emailValido(dto.getEmail())) {
            System.out.println(">> E-mail inválido.");
            return;
        }
=======
>>>>>>> 1931bad (corrigido edicao)
=======
>>>>>>> 1931bad (corrigido edicao)

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

<<<<<<< HEAD
<<<<<<< HEAD
        if (!confirmar("Confirmar inclusão?")) return;

=======
>>>>>>> 1931bad (corrigido edicao)
=======
>>>>>>> 1931bad (corrigido edicao)
        try {
            Usuario novo = usuarioService.create(dto);
            System.out.println(">> Usuário criado com ID " + novo.getId());
        } catch (Exception e) {
            System.out.println(">> Erro ao incluir: " + e.getMessage());
        }
    }

<<<<<<< HEAD
<<<<<<< HEAD
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

=======
=======
>>>>>>> 1931bad (corrigido edicao)
    private boolean alterarUsuario(long id) {
        System.out.println();
        System.out.println("--- Alterar Usuário ---");

        Optional<Usuario> opt = usuarioService.findById(id);
        if (opt.isEmpty()) {
            System.out.println(">> Usuário não encontrado.");
            return false;
        }

<<<<<<< HEAD
>>>>>>> 1931bad (corrigido edicao)
=======
>>>>>>> 1931bad (corrigido edicao)
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();

        System.out.print("Novo nome: ");
        dto.setNome(in.nextLine().trim());

        System.out.print("Novo CPF (000.000.000-00): ");
        String cpf = in.nextLine().trim();
        if (!CpfValidator.isValid(cpf)) {
            System.out.println(">> CPF inválido.");
<<<<<<< HEAD
<<<<<<< HEAD
            return;
=======
            return false;
>>>>>>> 1931bad (corrigido edicao)
=======
            return false;
>>>>>>> 1931bad (corrigido edicao)
        }
        dto.setCpf(cpf);

        System.out.print("Grupo [1-ADMINISTRADOR | 2-ESTOQUISTA]: ");
        int g = lerIntSeguro();
        dto.setGrupo(g == 1 ? Grupo.ADMINISTRADOR : Grupo.ESTOQUISTA);

<<<<<<< HEAD
<<<<<<< HEAD
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
=======
=======
>>>>>>> 1931bad (corrigido edicao)
        try {
            Usuario upd = usuarioService.update(id, dto);
            System.out.println(">> Usuário atualizado: " + upd.getNome());
            return true;
        } catch (Exception e) {
            System.out.println(">> Erro ao alterar: " + e.getMessage());
            return false;
        }
    }

    private void alterarSenha(long id) {
        System.out.println();
        System.out.println("--- Alterar Senha ---");

<<<<<<< HEAD
>>>>>>> 1931bad (corrigido edicao)
=======
>>>>>>> 1931bad (corrigido edicao)
        Optional<Usuario> opt = usuarioService.findById(id);
        if (opt.isEmpty()) {
            System.out.println(">> Usuário não encontrado.");
            return;
        }
<<<<<<< HEAD
<<<<<<< HEAD
        Usuario u = opt.get();
        printUsuario(u);
=======
>>>>>>> 1931bad (corrigido edicao)
=======
>>>>>>> 1931bad (corrigido edicao)

        AlterarSenhaDTO dto = new AlterarSenhaDTO();
        System.out.print("Nova senha: ");
        dto.setNovaSenha(in.nextLine().trim());
        System.out.print("Confirmar nova senha: ");
        dto.setConfirmarSenha(in.nextLine().trim());

        if (!dto.getNovaSenha().equals(dto.getConfirmarSenha())) {
            System.out.println(">> Senhas não conferem.");
            return;
        }

<<<<<<< HEAD
<<<<<<< HEAD
        if (!confirmar("Confirmar alteração de senha?")) return;

=======
>>>>>>> 1931bad (corrigido edicao)
=======
>>>>>>> 1931bad (corrigido edicao)
        try {
            usuarioService.alterarSenha(id, dto.getNovaSenha());
            System.out.println(">> Senha alterada com sucesso.");
        } catch (Exception e) {
            System.out.println(">> Erro ao alterar senha: " + e.getMessage());
        }
    }

<<<<<<< HEAD
<<<<<<< HEAD
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

=======
>>>>>>> 1931bad (corrigido edicao)
=======
>>>>>>> 1931bad (corrigido edicao)
    private boolean isAdmin(Usuario u) {
        return u.getGrupo() == Grupo.ADMINISTRADOR;
    }

    private void deny() {
        System.out.println("Acesso negado: apenas administradores.");
    }

<<<<<<< HEAD
<<<<<<< HEAD
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

=======
>>>>>>> 1931bad (corrigido edicao)
=======
>>>>>>> 1931bad (corrigido edicao)
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
<<<<<<< HEAD
<<<<<<< HEAD
=======
=======
>>>>>>> 1931bad (corrigido edicao)

    private void printSeparator() {
        System.out.println("---------------------------------------------------------------------");
    }

    private String crop(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, Math.max(0, max - 1)) + "…";
    }

    private String maskCpf(String cpf) {
        if (cpf == null) return "";
        String digits = cpf.replaceAll("\\D", "");
        if (digits.length() != 11) return cpf; 
        return String.format("%s.%s.%s-%s",
                digits.substring(0, 3),
                digits.substring(3, 6),
                digits.substring(6, 9),
                digits.substring(9));
    }
<<<<<<< HEAD
>>>>>>> 1931bad (corrigido edicao)
=======
>>>>>>> 1931bad (corrigido edicao)
}
