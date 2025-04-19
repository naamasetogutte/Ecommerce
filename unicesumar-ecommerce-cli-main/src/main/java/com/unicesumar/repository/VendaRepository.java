package com.unicesumar.repository;

import com.unicesumar.entities.User;
import com.unicesumar.entities.Venda;
import com.unicesumar.entities.Product;
import java.sql.*;
import java.util.*;

public class VendaRepository {
    private final Connection connection;

    public VendaRepository(Connection connection) {
        this.connection = connection;
    }

    public void salvar(Venda venda) throws SQLException {
        try {
            String sqlVenda = "INSERT INTO sales(id, user_id, payment_method) VALUES(?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sqlVenda)) {
                pstmt.setString(1, venda.getUuid().toString());
                pstmt.setString(2, venda.getUsuario().getUuid().toString());
                pstmt.setString(3, venda.getFormaDePagamento());
                pstmt.executeUpdate();
            }

            String sqlItens = "INSERT INTO sale_products(sale_id, product_id) VALUES(?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sqlItens)) {
                for (Product produto : venda.getProdutos()) {
                    pstmt.setString(1, venda.getUuid().toString());
                    pstmt.setString(2, produto.getUuid().toString());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public List<Venda> listarTodas() throws SQLException {
        List<Venda> vendas = new ArrayList<>();

        String sqlVendas = "SELECT s.id, s.payment_method, u.uuid as user_uuid, u.name as user_name " +
                "FROM sales s JOIN users u ON s.user_id = u.uuid";

        try (Statement stmt = connection.createStatement();
             ResultSet rsVendas = stmt.executeQuery(sqlVendas)) {

            while (rsVendas.next()) {
                Venda venda = new Venda(
                        new User(
                                UUID.fromString(rsVendas.getString("user_uuid")),
                                rsVendas.getString("user_name"),
                                "", ""
                        ),
                        new ArrayList<>(),
                        rsVendas.getString("payment_method")
                );
                venda.setUuid(UUID.fromString(rsVendas.getString("id")));

                carregarProdutosDaVenda(venda);
                vendas.add(venda);
            }
        }
        return vendas;
    }

    private void carregarProdutosDaVenda(Venda venda) throws SQLException {
        String sql = "SELECT p.uuid, p.name, p.price " +
                "FROM sale_products sp " +
                "JOIN products p ON sp.product_id = p.uuid " +
                "WHERE sp.sale_id = ?";

        List<Product> produtos = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, venda.getUuid().toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Product produto = new Product(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("name"),
                        rs.getDouble("price")
                );
                produtos.add(produto);
            }
        }

        venda.setProdutos(produtos);
    }
}