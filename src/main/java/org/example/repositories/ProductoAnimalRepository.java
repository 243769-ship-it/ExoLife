package org.example.repositories;

import org.example.models.ProductoAnimal;
import org.example.config.DBConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoAnimalRepository {

    private final Connection connection;

    public ProductoAnimalRepository(Connection connection) {
        this.connection = connection;
    }

    public ProductoAnimal save(ProductoAnimal pa) throws SQLException {
        String sql = "INSERT INTO producto_animal (producto_id, animal_id) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, pa.getProducto_id());
            stmt.setInt(2, pa.getAnimal_id());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    pa.setProducto_animal_id(generatedKeys.getInt(1));
                    return pa;
                }
            }
        }
        return null;
    }

    // --- MÉTODOS PARA ANIMALES (Checklist en Animal) ---
    public void deleteAllByAnimalId(Integer animalId) throws SQLException {
        String sql = "DELETE FROM producto_animal WHERE animal_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, animalId);
            stmt.executeUpdate();
        }
    }

    public List<Integer> findProductosIdByAnimalId(Integer animalId) throws SQLException {
        List<Integer> productosIds = new ArrayList<>();
        String sql = "SELECT producto_id FROM producto_animal WHERE animal_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, animalId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    productosIds.add(rs.getInt("producto_id"));
                }
            }
        }
        return productosIds;
    }

    // --- MÉTODOS PARA PRODUCTOS (Checklist en Producto) --- NUEVOS ---
    public void deleteAllByProductoId(Integer productoId) throws SQLException {
        String sql = "DELETE FROM producto_animal WHERE producto_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, productoId);
            stmt.executeUpdate();
        }
    }

    public List<Integer> findAnimalesIdByProductoId(Integer productoId) throws SQLException {
        List<Integer> animalIds = new ArrayList<>();
        String sql = "SELECT animal_id FROM producto_animal WHERE producto_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, productoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    animalIds.add(rs.getInt("animal_id"));
                }
            }
        }
        return animalIds;
    }
}