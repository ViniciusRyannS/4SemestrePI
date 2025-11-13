package br.com.julio.pi.backoffice_users.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import br.com.julio.pi.backoffice_users.model.ImagemProduto;
import br.com.julio.pi.backoffice_users.model.Produto;
import br.com.julio.pi.backoffice_users.model.enums.StatusProduto;
import br.com.julio.pi.backoffice_users.service.ProdutoService;

public class ProdutoConsoleController {

    private final ProdutoService produtoService;

    public ProdutoConsoleController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    public void menuAdmin(Scanner sc) {
        while (true) {
            listarProdutosAdmin(sc);
            return;
        }
    }

    private void listarProdutosAdmin(Scanner sc) {
        System.out.println();
        System.out.println("Listar Produtos");
        imprimirTabelaProdutos();

        System.out.print("Entre com o id para editar/ativar/inativar, 0 para voltar e i para incluir => ");
        String resp = sc.nextLine().trim();

        if (resp.equalsIgnoreCase("0") || resp.isBlank()) {
            return;
        }
        if (resp.equalsIgnoreCase("i")) {
            incluirProduto(sc);
            return;
        }

        try {
            long id = Long.parseLong(resp);
            submenuProdutoAdmin(sc, id);
        } catch (NumberFormatException e) {
            System.out.println("Opção inválida.");
        }
    }

    private void imprimirTabelaProdutos() {
        List<Produto> base = produtoService.listarPorNomeOuTodos(null);
        System.out.println("Id |   Nome              |  Quantidade  |   Valor    | status |");
        for (Produto p : base) {
            String nome = crop(p.getNome(), 16);
            System.out.printf("%d | %-16s | %10d | %9s | %-7s |%n",
                    p.getId(), nome, p.getQtdEstoque(), p.getPreco().toPlainString(),
                    p.getStatus().name().toLowerCase());
        }
    }

    private void submenuProdutoAdmin(Scanner sc, long id) {
        Produto p;
        try {
            p = produtoService.buscarPorIdComImagens(id); 
        } catch (NoSuchElementException e) {
            System.out.println(">> Produto não encontrado.");
            return;
        }

        while (true) {
            System.out.println();
            System.out.println("Opções de Produto");
            System.out.println("Opção de edição de Produto");
            System.out.println();
            imprimirDetalheProduto(p);
            System.out.println("--------------------------------------------------------------------");
            System.out.println("Opções");
            System.out.println("1) Alterar produto");
            System.out.println("2) Listar/alterar imagens do produto");
            System.out.println("3) Ativar/Desativar produto");
            System.out.println("4) Voltar Listar produto");
            System.out.print("Entre com a opção (1,2,3,4) => ");

            String opt = sc.nextLine().trim();
            if ("1".equals(opt)) {
                alterarProduto(sc, p.getId());
                p = produtoService.buscarPorIdComImagens(p.getId());
            } else if ("2".equals(opt)) {
                imagensProduto(sc, p.getId());
                p = produtoService.buscarPorIdComImagens(p.getId());
            } else if ("3".equals(opt)) {
                ativarDesativarProduto(sc, p.getId());
                p = produtoService.buscarPorIdComImagens(p.getId());
            } else if ("4".equals(opt)) {
                return;
            } else {
                System.out.println("Opção inválida.");
            }
        }
    }

    private void incluirProduto(Scanner sc) {
        System.out.println();
        System.out.println("Opções de Produto");
        System.out.println("Incluir Produto");
        System.out.println();
        Produto novo = new Produto();

        System.out.print("Nome Produto => ");
        novo.setNome(sc.nextLine());

        System.out.print("Preço => ");
        novo.setPreco(lerBig(sc));

        System.out.print("Qtd. Estoque => ");
        novo.setQtdEstoque(lerInt(sc));

        System.out.print("Descrição Detalhada => ");
        novo.setDescricaoDetalhada(sc.nextLine());

        System.out.print("Avaliação => ");
        novo.setAvaliacao(lerBig(sc));

        System.out.println("-------------------------------------------------------------------");
        System.out.print("Salvar (Y/N) => ");
        if (!confirm(sc)) {
            System.out.println("Inclusão cancelada.");
            return;
        }

        Produto salvo;
        try {
            salvo = produtoService.incluir(novo);
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
            return;
        }

        while (true) {
            System.out.println();
            System.out.println("Opções de Produto");
            System.out.println("Incluir Imagem");
            System.out.println();

            System.out.print("Nome arquivo (origem) => ");
            String caminhoOrigem = sc.nextLine().trim();

            System.out.print("Principal (S/N) => ");
            boolean principal = yes(sc);

            try {
                produtoService.adicionarImagemFromOrigem(salvo.getId(), caminhoOrigem, principal);
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }

            System.out.println("-------------------------------------------------------------------");
            System.out.println("Opções");
            System.out.println("1) Salvar e incluir +1 imagem de produto");
            System.out.println("2) Salvar e finalizar");
            System.out.println("3) Não salvar e finalizar");
            System.out.print("Entre com a opção (1,2,3) => ");
            String opt = sc.nextLine().trim();
            if ("1".equals(opt)) continue;
            if ("2".equals(opt) || "3".equals(opt)) break;
            System.out.println("Opção inválida.");
        }
    }

    private void alterarProduto(Scanner sc, long id) {
        Produto p = produtoService.buscarPorId(id);

        System.out.println();
        System.out.println("Opções de Produto");
        System.out.println("Alterar Produto");
        System.out.println();
        imprimirDetalheProduto(p);
        System.out.println("-------------------------------------------------------------------");

        System.out.print("Nome Produto => ");
        String nome = sc.nextLine();
        if (!nome.isBlank()) p.setNome(nome);

        System.out.print("Preço => ");
        String precoStr = sc.nextLine();
        if (!precoStr.isBlank()) p.setPreco(new BigDecimal(precoStr.replace(",", ".")));

        System.out.print("Qtd. Estoque => ");
        String qtdStr = sc.nextLine();
        if (!qtdStr.isBlank()) p.setQtdEstoque(Integer.parseInt(qtdStr));

        System.out.print("Descrição Detalhada => ");
        String desc = sc.nextLine();
        if (!desc.isBlank()) p.setDescricaoDetalhada(desc);

        System.out.print("Avaliação => ");
        String avStr = sc.nextLine();
        if (!avStr.isBlank()) p.setAvaliacao(new BigDecimal(avStr.replace(",", ".")));

        System.out.print("Salvar (Y/N) => ");
        if (!confirm(sc)) {
            System.out.println("Alteração cancelada.");
            return;
        }

        try {
            produtoService.alterar(id, p);
            System.out.println("Produto alterado com sucesso.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void ativarDesativarProduto(Scanner sc, long id) {
        Produto p = produtoService.buscarPorId(id);
        System.out.println();
        System.out.println("Opções de Produto");
        System.out.println("Ativar/Desativar Produto");
        System.out.println();
        imprimirDetalheProduto(p);
        System.out.println("--------------------------------------------------------------------");
        System.out.println(p.getStatus() == StatusProduto.INATIVO ? "<<Ativar Produto>>" : "<<Desativar Produto>>");
        System.out.print("Salvar alteração (Y/N) => ");
        if (!confirm(sc)) {
            System.out.println("Operação cancelada.");
            return;
        }
        if (p.getStatus() == StatusProduto.ATIVO) {
            produtoService.inativar(id);
        } else {
            produtoService.ativar(id);
        }
        System.out.println("Status atualizado.");
    }

    private void imagensProduto(Scanner sc, long produtoId) {
    while (true) {
        Produto p = produtoService.buscarPorIdComImagens(produtoId);
        System.out.println();
        System.out.println("Listar imagem de Produtos");
        System.out.println("Listar imagens do Produto " + p.getId());
        System.out.println();
        System.out.println("Id |   Nome Imagem    |  diretório Destino  |    Principal  |");
        for (ImagemProduto img : p.getImagens()) {
            String nome = extrairNome(img.getCaminhoDestino());
            String dir = extrairDiretorio(img.getCaminhoDestino());
            System.out.printf("%d | %-15s | %18s | %12s |%n",
                    img.getId(), nome, dir, (img.isPrincipal() ? "sim" : "não"));
        }
        System.out.println("|");
        System.out.print("Entre com o id para remover a imagem, 0 para voltar e i para incluir => ");

        String opt = sc.nextLine().trim();
        if ("0".equalsIgnoreCase(opt)) return;

        if ("i".equalsIgnoreCase(opt)) {
            System.out.println();
            System.out.println("Opções de Produto");
            System.out.println("Incluir Imagem");
            System.out.println();

            System.out.print("Nome arquivo (origem) => ");
            String caminhoOrigem = sc.nextLine().trim();
            System.out.print("Principal (S/N) => ");
            boolean principal = yes(sc);

            try {
                produtoService.adicionarImagemFromOrigem(produtoId, caminhoOrigem, principal);
                System.out.println(">> Imagem adicionada.");
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }

            System.out.println("-------------------------------------------------------------------");
            System.out.println("1) Incluir +1 imagem");
            System.out.println("2) Voltar");
            System.out.print("Entre com a opção (1,2) => ");
            String o2 = sc.nextLine().trim();
            if ("1".equals(o2)) continue;
            return;

        } else {
            try {
                long imgId = Long.parseLong(opt);
                boolean removed = produtoService.removerImagem(produtoId, imgId);
                if (removed) {
                    System.out.println(">> Imagem removida.");
                } else {
                    System.out.println(">> Nenhuma imagem com esse ID foi encontrada.");
                }
                System.out.print("Pressione ENTER para voltar...");
                sc.nextLine();
                return;

            } catch (NumberFormatException e) {
                System.out.println("Opção inválida.");
                System.out.println("Erro: " + e.getMessage());
                System.out.print("Pressione ENTER para voltar...");
                sc.nextLine();
                return;
            }
        }
    }
}

    public void menuEstoquista(Scanner sc) {
        while (true) {
            System.out.println();
            System.out.println("Listar Produtos Estoquista");
            imprimirTabelaProdutos();
            System.out.print("Entre com o id para editar, 0 para voltar => ");
            String resp = sc.nextLine().trim();

            if (resp.equalsIgnoreCase("0") || resp.isBlank()) return;

            try {
                long id = Long.parseLong(resp);
                alterarEstoqueEstoquista(sc, id);
            } catch (NumberFormatException e) {
                System.out.println("Opção inválida.");
            }
        }
    }

    private void alterarEstoqueEstoquista(Scanner sc, long id) {
        Produto p;
        try {
            p = produtoService.buscarPorId(id);
        } catch (NoSuchElementException e) {
            System.out.println(">> Produto não encontrado.");
            return;
        }

        System.out.println();
        System.out.println("Opções de Produto Estoquista");
        System.out.println("Alterar Produto Estoquista");
        System.out.println();
        imprimirDetalheProduto(p);
        System.out.println("-------------------------------------------------------------------");
        System.out.print("Qtd. Estoque => ");
        int novo = lerInt(sc);
        System.out.print("Salvar (Y/N) => ");
        if (!confirm(sc)) {
            System.out.println("Alteração cancelada.");
            return;
        }
        try {
            produtoService.alterarEstoque(id, novo);
            System.out.println("Estoque atualizado.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void imprimirDetalheProduto(Produto p) {
        System.out.println("Id: " + p.getId());
        System.out.println("Nome Produto : " + p.getNome());
        System.out.println("Preço : " + p.getPreco().toPlainString());
        System.out.println("Qtd. Estoque : " + p.getQtdEstoque());
        System.out.println("Avaliação : " + p.getAvaliacao());
        System.out.println("Status => " + p.getStatus().name().toLowerCase());
    }

    private boolean confirm(Scanner sc) {
        String s = sc.nextLine().trim();
        return s.equalsIgnoreCase("y") || s.equalsIgnoreCase("s");
    }

    private boolean yes(Scanner sc) {
        String s = sc.nextLine().trim();
        return s.equalsIgnoreCase("s") || s.equalsIgnoreCase("y");
    }

    private BigDecimal lerBig(Scanner sc) {
        while (true) {
            String s = sc.nextLine().trim().replace(",", ".");
            try { return new BigDecimal(s); }
            catch (Exception e) { System.out.print("Valor inválido. Tente novamente: "); }
        }
    }

    private int lerInt(Scanner sc) {
        while (true) {
            String s = sc.nextLine().trim();
            try { return Integer.parseInt(s); }
            catch (Exception e) { System.out.print("Inteiro inválido. Tente novamente: "); }
        }
    }

    private String crop(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, Math.max(0, max - 1)) + "…";
    }

    private String extrairNome(String caminho) {
        if (caminho == null || caminho.isBlank()) return "";
        int i = caminho.lastIndexOf('/');
        return i >= 0 && i < caminho.length() - 1 ? caminho.substring(i + 1) : caminho;
    }

    private String extrairDiretorio(String caminho) {
        if (caminho == null || caminho.isBlank()) return "";
        int i = caminho.lastIndexOf('/');
        return i > 0 ? caminho.substring(0, i) : caminho;
    }
}
