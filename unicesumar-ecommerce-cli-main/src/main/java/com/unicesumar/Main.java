package com.unicesumar;

import com.unicesumar.entities.*;
import com.unicesumar.paymentMethods.*;
import com.unicesumar.repository.*;
import java.sql.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Connection conn = null;
        Scanner scanner = new Scanner(System.in);

        try {
            conn = DriverManager.getConnection("jdbc:sqlite:database.sqlite");
            conn.setAutoCommit(false);

            ProductRepository produtoRepo = new ProductRepository(conn);
            UserRepository usuarioRepo = new UserRepository(conn);
            VendaRepository vendaRepo = new VendaRepository(conn);

            int option;
            do {
                System.out.println("\n---------MENU---------");
                System.out.println("1 - Cadastrar Produto");
                System.out.println("2 - Listar Produtos");
                System.out.println("3 - Cadastrar Usuário");
                System.out.println("4 - Listar Usuários");
                System.out.println("5 - Listar Vendas");
                System.out.println("6 - Registrar Venda");
                System.out.println("7 - Sair");
                System.out.print("Escolha uma opção: ");

                option = scanner.nextInt();
                scanner.nextLine();

                switch (option) {
                    case 1:
                        cadastrarProduto(scanner, produtoRepo, conn);
                        break;
                    case 2:
                        listarProdutos(produtoRepo);
                        break;
                    case 3:
                        cadastrarUsuario(scanner, usuarioRepo, conn);
                        break;
                    case 4:
                        listarUsuarios(usuarioRepo);
                        break;
                    case 5:
                        listarVendas(vendaRepo);
                        break;
                    case 6:
                        registrarVenda(scanner, usuarioRepo, produtoRepo, vendaRepo);
                        break;
                    case 7:
                        System.out.println("Saindo do sistema...");
                        break;
                    default:
                        System.out.println("Opção inválida!");
                }
            } while (option != 7);

        } catch (SQLException e) {
            System.err.println("Erro no banco de dados: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Erro ao fechar conexão: " + e.getMessage());
                }
            }
            scanner.close();
        }
    }

    private static void cadastrarProduto(Scanner scanner, ProductRepository repo, Connection conn) {
        System.out.print("\nNome do produto: ");
        String nome = scanner.nextLine();

        System.out.print("Preço: ");
        double preco = scanner.nextDouble();
        scanner.nextLine();

        try {
            repo.save(new Product(nome, preco));
            conn.commit();
            System.out.println("Produto cadastrado com sucesso!");
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Erro ao fazer rollback: " + ex.getMessage());
            }
            System.out.println("Erro ao cadastrar produto: " + e.getMessage());
        }
    }

    private static void listarProdutos(ProductRepository repo) {
        try {
            List<Product> produtos = repo.findAll();
            if (produtos.isEmpty()) {
                System.out.println("\nNenhum produto cadastrado.");
            } else {
                System.out.println("\n--- PRODUTOS ---");
                produtos.forEach(System.out::println);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar produtos: " + e.getMessage());
        }
    }

    private static void cadastrarUsuario(Scanner scanner, UserRepository repo, Connection conn) {
        System.out.print("\nNome: ");
        String nome = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        try {
            repo.save(new User(nome, email, senha));
            conn.commit();
            System.out.println("Usuário cadastrado com sucesso!");
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Erro ao fazer rollback: " + ex.getMessage());
            }
            System.out.println("Erro ao cadastrar usuário: " + e.getMessage());
        }
    }

    private static void listarUsuarios(UserRepository repo) {
        try {
            List<User> usuarios = repo.findAll();
            if (usuarios.isEmpty()) {
                System.out.println("\nNenhum usuário cadastrado.");
            } else {
                System.out.println("\n--- USUÁRIOS ---");
                usuarios.forEach(System.out::println);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar usuários: " + e.getMessage());
        }
    }

    private static void listarVendas(VendaRepository repo) {
        try {
            List<Venda> vendas = repo.listarTodas();
            if (vendas.isEmpty()) {
                System.out.println("\nNenhuma venda registrada.");
            } else {
                System.out.println("\n--- VENDAS ---");
                vendas.forEach(v -> {
                    System.out.println("\nID: " + v.getUuid());
                    System.out.println("Cliente: " + v.getUsuario().getName());
                    System.out.println("Forma de Pagamento: " + v.getFormaDePagamento());
                    System.out.println("Produtos:");
                    v.getProdutos().forEach(p ->
                            System.out.println("- " + p.getName() + " (R$ " + p.getPrice() + ")"));
                    System.out.println("Total: R$ " + v.getValorTotal());
                });
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar vendas: " + e.getMessage());
        }
    }

    private static void registrarVenda(Scanner scanner, UserRepository userRepo,
                                       ProductRepository prodRepo, VendaRepository vendaRepo) {
        try {
            System.out.print("\nEmail do usuário: ");
            String email = scanner.nextLine();

            Optional<User> usuarioOpt = userRepo.findByEmail(email);
            if (!usuarioOpt.isPresent()) {
                System.out.println("Usuário não encontrado!");
                return;
            }
            User usuario = usuarioOpt.get();

            List<Product> produtos = new ArrayList<>();
            listarProdutos(prodRepo);

            while (true) {
                System.out.print("\nID do produto (ou 'fim'): ");
                String input = scanner.nextLine();

                if (input.equalsIgnoreCase("fim")) break;

                try {
                    UUID id = UUID.fromString(input);
                    Optional<Product> produto = prodRepo.findById(id);

                    if (produto.isPresent()) {
                        produtos.add(produto.get());
                        System.out.println("Produto adicionado: " + produto.get().getName());
                    } else {
                        System.out.println("Produto não encontrado!");
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("ID inválido!");
                }
            }

            if (produtos.isEmpty()) {
                System.out.println("Nenhum produto selecionado!");
                return;
            }

            System.out.println("\nFORMAS DE PAGAMENTO:");
            System.out.println("1 - PIX");
            System.out.println("2 - Cartão");
            System.out.println("3 - Boleto");
            System.out.print("Escolha: ");

            int opcao = scanner.nextInt();
            scanner.nextLine();

            PaymentType tipo;
            switch (opcao) {
                case 2:
                    tipo = PaymentType.CARTAO;
                    break;
                case 3:
                    tipo = PaymentType.BOLETO;
                    break;
                default:
                    tipo = PaymentType.PIX;
            }

            double total = produtos.stream().mapToDouble(Product::getPrice).sum();
            System.out.printf("\nTotal: R$ %.2f%n", total);
            System.out.print("Confirmar venda? (S/N): ");
            String confirmacao = scanner.nextLine();

            if (!confirmacao.equalsIgnoreCase("s")) {
                System.out.println("Venda cancelada!");
                return;
            }

            PaymentMethod metodo = PaymentMethodFactory.create(tipo);
            metodo.pay(total);

            Venda venda = new Venda(usuario, produtos, tipo.toString());
            vendaRepo.salvar(venda);

            System.out.println("Venda registrada com sucesso! ID: " + venda.getUuid());

        } catch (SQLException e) {
            System.out.println("Erro ao registrar venda: " + e.getMessage());
        }
    }
}