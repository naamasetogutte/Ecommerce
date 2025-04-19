package com.unicesumar.repository;

import com.unicesumar.entities.Product;
import java.sql.*;
import java.util.*;

public class ProductRepository {
    private final Connection connection;

    public ProductRepository(Connection connection) {
        this.connection = connection;
    }

    public void save(Product product) throws SQLException {
        String sql = "INSERT INTO products(uuid, name, price) VALUES(?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, product.getUuid().toString());
            pstmt.setString(2, product.getName());
            pstmt.setDouble(3, product.getPrice());
            pstmt.executeUpdate();
        }
    }

    public Optional<Product> findById(UUID id) throws SQLException {
        String sql = "SELECT * FROM products WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new Product(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("name"),
                        rs.getDouble("price")
                ));
            }
            return Optional.empty();
        }
    }

    public List<Product> findAll() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(new Product(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("name"),
                        rs.getDouble("price")
                ));
            }
        }
        return products;
    }
}